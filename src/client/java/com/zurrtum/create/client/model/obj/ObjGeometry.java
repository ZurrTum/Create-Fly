/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.zurrtum.create.client.model.obj;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.zurrtum.create.client.model.ExtendedUnbakedGeometry;
import com.zurrtum.create.client.model.NeoForgeModelProperties;
import com.zurrtum.create.client.model.StandardModelParameters;
import joptsimple.internal.Strings;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.model.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.context.ContextParameterMap;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.IOException;
import java.util.*;

public class ObjGeometry implements ExtendedUnbakedGeometry {
    private static final Vector4f COLOR_WHITE = new Vector4f(1, 1, 1, 1);
    private static final Vec2f[] DEFAULT_COORDS = {new Vec2f(0, 0), new Vec2f(0, 1), new Vec2f(1, 1), new Vec2f(1, 0),};

    private final Multimap<String, ModelGroup> parts = MultimapBuilder.linkedHashKeys().arrayListValues().build();

    private final List<Vector3f> positions = Lists.newArrayList();
    private final List<Vec2f> texCoords = Lists.newArrayList();
    private final List<Vector3f> normals = Lists.newArrayList();
    private final List<Vector4f> colors = Lists.newArrayList();

    public final boolean automaticCulling;
    public final boolean shadeQuads;
    public final boolean flipV;
    public final boolean emissiveAmbient;
    @Nullable
    public final String mtlOverride;
    public final StandardModelParameters parameters;

    public final Identifier modelLocation;

    private ObjGeometry(Settings settings) {
        this.modelLocation = settings.modelLocation();
        this.automaticCulling = settings.automaticCulling();
        this.shadeQuads = settings.shadeQuads();
        this.flipV = settings.flipV();
        this.emissiveAmbient = settings.emissiveAmbient();
        this.mtlOverride = settings.mtlOverride();
        this.parameters = settings.parameters();
    }

    public static ObjGeometry parse(ObjTokenizer tokenizer, Settings settings) throws IOException {
        var modelLocation = settings.modelLocation();
        var materialLibraryOverrideLocation = settings.mtlOverride();
        var model = new ObjGeometry(settings);

        // for relative references to material libraries
        String modelDomain = modelLocation.getNamespace();
        String modelPath = modelLocation.getPath();
        int lastSlash = modelPath.lastIndexOf('/');
        if (lastSlash >= 0)
            modelPath = modelPath.substring(0, lastSlash + 1); // include the '/'
        else
            modelPath = "";

        ObjMaterialLibrary mtllib = ObjMaterialLibrary.EMPTY;
        ObjMaterialLibrary.Material currentMat = null;
        String currentSmoothingGroup = null;
        ModelGroup currentGroup = null;
        ModelObject currentObject = null;
        ModelMesh currentMesh = null;

        boolean objAboveGroup = false;

        if (materialLibraryOverrideLocation != null) {
            String lib = materialLibraryOverrideLocation;
            if (lib.contains(":"))
                mtllib = ObjLoader.INSTANCE.loadMaterialLibrary(Identifier.of(lib));
            else
                mtllib = ObjLoader.INSTANCE.loadMaterialLibrary(Identifier.of(modelDomain, modelPath + lib));
        }

        String[] line;
        while ((line = tokenizer.readAndSplitLine(true)) != null) {
            switch (line[0]) {
                case "mtllib": // Loads material library
                {
                    if (materialLibraryOverrideLocation != null)
                        break;

                    String lib = line[1];
                    if (lib.contains(":"))
                        mtllib = ObjLoader.INSTANCE.loadMaterialLibrary(Identifier.of(lib));
                    else
                        mtllib = ObjLoader.INSTANCE.loadMaterialLibrary(Identifier.of(modelDomain, modelPath + lib));
                    break;
                }

                case "usemtl": // Sets the current material (starts new mesh)
                {
                    String mat = Strings.join(Arrays.copyOfRange(line, 1, line.length), " ");
                    ObjMaterialLibrary.Material newMat = mtllib.getMaterial(mat);
                    if (!Objects.equals(newMat, currentMat)) {
                        currentMat = newMat;
                        if (currentMesh != null && currentMesh.mat == null && currentMesh.faces.size() == 0) {
                            currentMesh.mat = currentMat;
                        } else {
                            // Start new mesh
                            currentMesh = null;
                        }
                    }
                    break;
                }

                case "v": // Vertex
                    model.positions.add(parseVector4To3(line));
                    break;
                case "vt": // Vertex texcoord
                    model.texCoords.add(parseVector2(line));
                    break;
                case "vn": // Vertex normal
                    model.normals.add(parseVector3(line));
                    break;
                case "vc": // Vertex color (non-standard)
                    model.colors.add(parseVector4(line));
                    break;

                case "f": // Face
                {
                    if (currentMesh == null) {
                        currentMesh = model.new ModelMesh(currentMat, currentSmoothingGroup);
                        if (currentObject != null) {
                            currentObject.meshes.add(currentMesh);
                        } else {
                            if (currentGroup == null) {
                                currentGroup = model.new ModelGroup("");
                                model.parts.put("", currentGroup);
                            }
                            currentGroup.meshes.add(currentMesh);
                        }
                    }

                    int[][] vertices = new int[line.length - 1][];
                    for (int i = 0; i < vertices.length; i++) {
                        String vertexData = line[i + 1];
                        String[] vertexParts = vertexData.split("/");
                        int[] vertex = Arrays.stream(vertexParts).mapToInt(num -> Strings.isNullOrEmpty(num) ? 0 : Integer.parseInt(num)).toArray();
                        if (vertex[0] < 0)
                            vertex[0] = model.positions.size() + vertex[0];
                        else
                            vertex[0]--;
                        if (vertex.length > 1) {
                            if (vertex[1] < 0)
                                vertex[1] = model.texCoords.size() + vertex[1];
                            else
                                vertex[1]--;
                            if (vertex.length > 2) {
                                if (vertex[2] < 0)
                                    vertex[2] = model.normals.size() + vertex[2];
                                else
                                    vertex[2]--;
                                if (vertex.length > 3) {
                                    if (vertex[3] < 0)
                                        vertex[3] = model.colors.size() + vertex[3];
                                    else
                                        vertex[3]--;
                                }
                            }
                        }
                        vertices[i] = vertex;
                    }

                    currentMesh.faces.add(vertices);

                    break;
                }

                case "s": // Smoothing group (starts new mesh)
                {
                    String smoothingGroup = "off".equals(line[1]) ? null : line[1];
                    if (!Objects.equals(currentSmoothingGroup, smoothingGroup)) {
                        currentSmoothingGroup = smoothingGroup;
                        if (currentMesh != null && currentMesh.smoothingGroup == null && currentMesh.faces.size() == 0) {
                            currentMesh.smoothingGroup = currentSmoothingGroup;
                        } else {
                            // Start new mesh
                            currentMesh = null;
                        }
                    }
                    break;
                }

                case "g": {
                    String name = line[1];
                    if (objAboveGroup) {
                        currentObject = model.new ModelObject(currentGroup.name() + "/" + name);
                        currentGroup.parts.put(name, currentObject);
                    } else {
                        currentGroup = model.new ModelGroup(name);
                        model.parts.put(name, currentGroup);
                        currentObject = null;
                    }
                    // Start new mesh
                    currentMesh = null;
                    break;
                }

                case "o": {
                    String name = line[1];
                    if (objAboveGroup || currentGroup == null) {
                        objAboveGroup = true;

                        currentGroup = model.new ModelGroup(name);
                        model.parts.put(name, currentGroup);
                        currentObject = null;
                    } else {
                        currentObject = model.new ModelObject(currentGroup.name() + "/" + name);
                        currentGroup.parts.put(name, currentObject);
                    }
                    // Start new mesh
                    currentMesh = null;
                    break;
                }
            }
        }
        return model;
    }

    private static Vector3f parseVector4To3(String[] line) {
        Vector4f vec4 = parseVector4(line);
        return new Vector3f(vec4.x() / vec4.w(), vec4.y() / vec4.w(), vec4.z() / vec4.w());
    }

    private static Vec2f parseVector2(String[] line) {
        return switch (line.length) {
            case 1 -> new Vec2f(0, 0);
            case 2 -> new Vec2f(Float.parseFloat(line[1]), 0);
            default -> new Vec2f(Float.parseFloat(line[1]), Float.parseFloat(line[2]));
        };
    }

    private static Vector3f parseVector3(String[] line) {
        return switch (line.length) {
            case 1 -> new Vector3f();
            case 2 -> new Vector3f(Float.parseFloat(line[1]), 0, 0);
            case 3 -> new Vector3f(Float.parseFloat(line[1]), Float.parseFloat(line[2]), 0);
            default -> new Vector3f(Float.parseFloat(line[1]), Float.parseFloat(line[2]), Float.parseFloat(line[3]));
        };
    }

    static Vector4f parseVector4(String[] line) {
        return switch (line.length) {
            case 1 -> new Vector4f();
            case 2 -> new Vector4f(Float.parseFloat(line[1]), 0, 0, 1);
            case 3 -> new Vector4f(Float.parseFloat(line[1]), Float.parseFloat(line[2]), 0, 1);
            case 4 -> new Vector4f(Float.parseFloat(line[1]), Float.parseFloat(line[2]), Float.parseFloat(line[3]), 1);
            default -> new Vector4f(Float.parseFloat(line[1]), Float.parseFloat(line[2]), Float.parseFloat(line[3]), Float.parseFloat(line[4]));
        };
    }

    @Override
    public BakedGeometry bake(ModelTextures textureSlots, Baker baker, ModelBakeSettings state, SimpleModel debugName) {
        ContextParameterMap.Builder propertiesBuilder = new ContextParameterMap.Builder();
        NeoForgeModelProperties.fillRootTransformProperty(propertiesBuilder, parameters.rootTransform());
        NeoForgeModelProperties.fillPartVisibilityProperty(propertiesBuilder, parameters.partVisibility());
        return bake(textureSlots, baker, state, debugName, propertiesBuilder.build(NeoForgeModelProperties.TYPE));
    }

    @Override
    public BakedGeometry bake(
        ModelTextures textureSlots,
        Baker baker,
        ModelBakeSettings state,
        SimpleModel debugName,
        ContextParameterMap additionalProperties
    ) {
        Map<String, Boolean> partVisibility = additionalProperties.getOrDefault(NeoForgeModelProperties.PART_VISIBILITY, Map.of());
        var builder = new BakedGeometry.Builder();
        parts.values().stream().filter(part -> partVisibility.getOrDefault(part.name(), true))
            .forEach(part -> part.addQuads(builder, textureSlots, baker, state, debugName, additionalProperties));
        return builder.build();
    }

    private AffineTransformation blockCenterToCorner(AffineTransformation transform) {
        if (transform.equals(AffineTransformation.identity()))
            return AffineTransformation.identity();

        Matrix4f ret = transform.copyMatrix();
        Vector3f origin = new Vector3f(.5f, .5f, .5f);
        Matrix4f tmp = new Matrix4f().translation(origin.x(), origin.y(), origin.z());
        tmp.mul(ret, ret);
        tmp.translation(-origin.x(), -origin.y(), -origin.z());
        ret.mul(tmp);
        return new AffineTransformation(ret);
    }

    private Pair<BakedQuad, Direction> makeQuad(
        int[][] indices,
        int tintIndex,
        Vector4f colorTint,
        Vector4f ambientColor,
        Sprite texture,
        AffineTransformation transform
    ) {
        boolean needsNormalRecalculation = false;
        for (int[] ints : indices) {
            needsNormalRecalculation |= ints.length < 3;
        }
        Vector3f faceNormal = new Vector3f();
        if (needsNormalRecalculation) {
            Vector3f a = positions.get(indices[0][0]);
            Vector3f ab = positions.get(indices[1][0]);
            Vector3f ac = positions.get(indices[2][0]);
            Vector3f abs = new Vector3f(ab);
            abs.sub(a);
            Vector3f acs = new Vector3f(ac);
            acs.sub(a);
            abs.cross(acs);
            abs.normalize();
            faceNormal = abs;
        }

        var quadBaker = new QuadBakingVertexConsumer();

        quadBaker.setSprite(texture);
        quadBaker.setTintIndex(tintIndex);

        int uv2 = 0;
        if (emissiveAmbient) {
            int fakeLight = (int) ((ambientColor.x() + ambientColor.y() + ambientColor.z()) * 15 / 3.0f);
            uv2 = LightmapTextureManager.pack(fakeLight, fakeLight);
            quadBaker.setShade(fakeLight == 0 && shadeQuads);
        } else {
            quadBaker.setShade(shadeQuads);
        }

        boolean hasTransform = !transform.equals(AffineTransformation.identity());
        // The incoming transform is referenced on the center of the block, but our coords are referenced on the corner
        AffineTransformation transformation = hasTransform ? blockCenterToCorner(transform) : transform;

        Vector4f[] pos = new Vector4f[4];
        Vector3f[] norm = new Vector3f[4];

        for (int i = 0; i < 4; i++) {
            int[] index = indices[Math.min(i, indices.length - 1)];
            Vector4f position = new Vector4f(positions.get(index[0]), 1);
            Vec2f texCoord = index.length >= 2 && texCoords.size() > 0 ? texCoords.get(index[1]) : DEFAULT_COORDS[i];
            Vector3f norm0 = !needsNormalRecalculation && index.length >= 3 && normals.size() > 0 ? normals.get(index[2]) : faceNormal;
            Vector3f normal = norm0;
            Vector4f color = index.length >= 4 && colors.size() > 0 ? colors.get(index[3]) : COLOR_WHITE;
            if (hasTransform) {
                normal = new Vector3f(norm0);
                position.mul(transformation.getMatrix());
                Matrix3f normalTransform = new Matrix3f(transformation.getMatrix());
                normalTransform.invert();
                normalTransform.transpose();
                normal.mul(normalTransform);
                normal.normalize();
            }
            Vector4f tintedColor = new Vector4f(
                color.x() * colorTint.x(),
                color.y() * colorTint.y(),
                color.z() * colorTint.z(),
                color.w() * colorTint.w()
            );
            quadBaker.vertex(position.x(), position.y(), position.z());
            quadBaker.color(tintedColor.x(), tintedColor.y(), tintedColor.z(), tintedColor.w());
            quadBaker.texture(texture.getFrameU(texCoord.x), texture.getFrameV((flipV ? 1 - texCoord.y : texCoord.y)));
            quadBaker.light(uv2);
            quadBaker.normal(normal.x(), normal.y(), normal.z());
            if (i == 0) {
                quadBaker.setDirection(Direction.getFacing(normal.x(), normal.y(), normal.z()));
            }
            pos[i] = position;
            norm[i] = normal;
        }

        Direction cull = null;
        if (automaticCulling) {
            if (MathHelper.approximatelyEquals(pos[0].x(), 0) && // vertex.position.x
                MathHelper.approximatelyEquals(pos[1].x(), 0) && MathHelper.approximatelyEquals(
                pos[2].x(),
                0
            ) && MathHelper.approximatelyEquals(pos[3].x(), 0) && norm[0].x() < 0) // vertex.normal.x
            {
                cull = Direction.WEST;
            } else if (MathHelper.approximatelyEquals(pos[0].x(), 1) && // vertex.position.x
                MathHelper.approximatelyEquals(pos[1].x(), 1) && MathHelper.approximatelyEquals(
                pos[2].x(),
                1
            ) && MathHelper.approximatelyEquals(pos[3].x(), 1) && norm[0].x() > 0) // vertex.normal.x
            {
                cull = Direction.EAST;
            } else if (MathHelper.approximatelyEquals(pos[0].z(), 0) && // vertex.position.z
                MathHelper.approximatelyEquals(pos[1].z(), 0) && MathHelper.approximatelyEquals(
                pos[2].z(),
                0
            ) && MathHelper.approximatelyEquals(pos[3].z(), 0) && norm[0].z() < 0) // vertex.normal.z
            {
                cull = Direction.NORTH; // can never remember
            } else if (MathHelper.approximatelyEquals(pos[0].z(), 1) && // vertex.position.z
                MathHelper.approximatelyEquals(pos[1].z(), 1) && MathHelper.approximatelyEquals(
                pos[2].z(),
                1
            ) && MathHelper.approximatelyEquals(pos[3].z(), 1) && norm[0].z() > 0) // vertex.normal.z
            {
                cull = Direction.SOUTH;
            } else if (MathHelper.approximatelyEquals(pos[0].y(), 0) && // vertex.position.y
                MathHelper.approximatelyEquals(pos[1].y(), 0) && MathHelper.approximatelyEquals(
                pos[2].y(),
                0
            ) && MathHelper.approximatelyEquals(pos[3].y(), 0) && norm[0].y() < 0) // vertex.normal.z
            {
                cull = Direction.DOWN; // can never remember
            } else if (MathHelper.approximatelyEquals(pos[0].y(), 1) && // vertex.position.y
                MathHelper.approximatelyEquals(pos[1].y(), 1) && MathHelper.approximatelyEquals(
                pos[2].y(),
                1
            ) && MathHelper.approximatelyEquals(pos[3].y(), 1) && norm[0].y() > 0) // vertex.normal.y
            {
                cull = Direction.UP;
            }
        }

        return Pair.of(quadBaker.bakeQuad(), cull);
    }

    public class ModelObject {
        public final String name;

        List<ModelMesh> meshes = Lists.newArrayList();

        ModelObject(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }

        public void addQuads(
            BakedGeometry.Builder builder,
            ModelTextures slots,
            Baker baker,
            ModelBakeSettings state,
            SimpleModel debugName,
            ContextParameterMap additionalProperties
        ) {
            for (ModelMesh mesh : meshes) {
                mesh.addQuads(builder, slots, baker, state, debugName, additionalProperties);
            }
        }

        protected void addNamesRecursively(Set<String> names) {
            names.add(name());
        }
    }

    public class ModelGroup extends ModelObject {
        final Multimap<String, ModelObject> parts = MultimapBuilder.linkedHashKeys().arrayListValues().build();

        ModelGroup(String name) {
            super(name);
        }

        @Override
        public void addQuads(
            BakedGeometry.Builder builder,
            ModelTextures slots,
            Baker baker,
            ModelBakeSettings state,
            SimpleModel debugName,
            ContextParameterMap additionalProperties
        ) {
            super.addQuads(builder, slots, baker, state, debugName, additionalProperties);

            Map<String, Boolean> partVisibility = additionalProperties.getOrDefault(NeoForgeModelProperties.PART_VISIBILITY, Map.of());
            parts.values().stream().filter(part -> partVisibility.getOrDefault("%s.%s".formatted(name(), part.name()), true))
                .forEach(part -> part.addQuads(builder, slots, baker, state, debugName, additionalProperties));
        }

        @Override
        protected void addNamesRecursively(Set<String> names) {
            super.addNamesRecursively(names);
            for (ModelObject object : parts.values())
                object.addNamesRecursively(names);
        }
    }

    private class ModelMesh {
        @Nullable
        public ObjMaterialLibrary.Material mat;
        @Nullable
        public String smoothingGroup;
        public final List<int[][]> faces = Lists.newArrayList();

        public ModelMesh(@Nullable ObjMaterialLibrary.Material currentMat, @Nullable String currentSmoothingGroup) {
            this.mat = currentMat;
            this.smoothingGroup = currentSmoothingGroup;
        }

        public void addQuads(
            BakedGeometry.Builder builder,
            ModelTextures slots,
            Baker baker,
            ModelBakeSettings state,
            SimpleModel debugName,
            ContextParameterMap additionalProperties
        ) {
            if (mat == null)
                return;
            Sprite texture = baker.getSpriteGetter().get(slots, mat.diffuseColorMap, debugName);
            int tintIndex = mat.diffuseTintIndex;
            Vector4f colorTint = mat.diffuseColor;

            var rootTransform = additionalProperties.getOrDefault(NeoForgeModelProperties.TRANSFORM, AffineTransformation.identity());
            var transform = rootTransform.equals(AffineTransformation.identity()) ? state.getRotation() : state.getRotation().multiply(rootTransform);
            for (int[][] face : faces) {
                Pair<BakedQuad, Direction> quad = makeQuad(face, tintIndex, colorTint, mat.ambientColor, texture, transform);
                if (quad.getRight() == null)
                    builder.add(quad.getLeft());
                else
                    builder.add(quad.getRight(), quad.getLeft());
            }
        }
    }

    public record Settings(
        Identifier modelLocation, boolean automaticCulling, boolean shadeQuads, boolean flipV, boolean emissiveAmbient, @Nullable String mtlOverride,
        StandardModelParameters parameters
    ) {
    }
}

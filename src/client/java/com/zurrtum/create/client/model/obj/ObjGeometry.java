/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.zurrtum.create.client.model.obj;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.mojang.blaze3d.platform.Transparency;
import com.mojang.math.Transformation;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import com.zurrtum.create.client.model.ExtendedUnbakedGeometry;
import com.zurrtum.create.client.model.NeoForgeModelProperties;
import com.zurrtum.create.client.model.StandardModelParameters;
import com.zurrtum.create.client.model.obj.ObjMaterialLibrary.Material;
import joptsimple.internal.Strings;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBaker.Interner;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material.Baked;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.phys.Vec2;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.*;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.lang.Math;
import java.util.*;

public class ObjGeometry implements ExtendedUnbakedGeometry {
    private static final Vec2[] DEFAULT_COORDS = {new Vec2(0, 0), new Vec2(0, 1), new Vec2(1, 1), new Vec2(1, 0),};

    private final Multimap<String, ModelGroup> parts = MultimapBuilder.linkedHashKeys().arrayListValues().build();

    private final List<Vector3f> positions = Lists.newArrayList();
    private final List<Vec2> texCoords = Lists.newArrayList();
    private final List<Vector3f> normals = Lists.newArrayList();
    private final List<Vector4f> colors = Lists.newArrayList();

    public final boolean automaticCulling;
    public final boolean shadeQuads;
    public final boolean emissiveAmbient;
    @Nullable
    public final String mtlOverride;
    public final StandardModelParameters parameters;

    public final Identifier modelLocation;

    private ObjGeometry(Settings settings) {
        modelLocation = settings.modelLocation();
        automaticCulling = settings.automaticCulling();
        shadeQuads = settings.shadeQuads();
        emissiveAmbient = settings.emissiveAmbient();
        mtlOverride = settings.mtlOverride();
        parameters = settings.parameters();
    }

    public static ObjGeometry parse(ObjTokenizer tokenizer, Settings settings) throws IOException {
        Identifier modelLocation = settings.modelLocation();
        String materialLibraryOverrideLocation = settings.mtlOverride();
        boolean flipV = settings.flipV();
        ObjGeometry model = new ObjGeometry(settings);

        // for relative references to material libraries
        String modelDomain = modelLocation.getNamespace();
        String modelPath = modelLocation.getPath();
        int lastSlash = modelPath.lastIndexOf('/');
        if (lastSlash >= 0) {
            modelPath = modelPath.substring(0, lastSlash + 1); // include the '/'
        } else {
            modelPath = "";
        }

        ObjMaterialLibrary mtllib = ObjMaterialLibrary.EMPTY;
        Material currentMat = null;
        String currentSmoothingGroup = null;
        ModelGroup currentGroup = null;
        ModelObject currentObject = null;
        ModelMesh currentMesh = null;

        boolean objAboveGroup = false;

        if (materialLibraryOverrideLocation != null) {
            String lib = materialLibraryOverrideLocation;
            if (lib.contains(":")) {
                mtllib = ObjLoader.INSTANCE.loadMaterialLibrary(Identifier.parse(lib));
            } else {
                mtllib = ObjLoader.INSTANCE.loadMaterialLibrary(Identifier.fromNamespaceAndPath(
                    modelDomain,
                    modelPath + lib
                ));
            }
        }

        String[] line;
        while ((line = tokenizer.readAndSplitLine(true)) != null) {
            switch (line[0]) {
                case "mtllib": // Loads material library
                    if (materialLibraryOverrideLocation != null) {
                        break;
                    }

                    String lib = line[1];
                    if (lib.contains(":")) {
                        mtllib = ObjLoader.INSTANCE.loadMaterialLibrary(Identifier.parse(lib));
                    } else {
                        mtllib = ObjLoader.INSTANCE.loadMaterialLibrary(Identifier.fromNamespaceAndPath(
                            modelDomain,
                            modelPath + lib
                        ));
                    }
                    break;

                case "usemtl": // Sets the current material (starts new mesh)
                    String mat = Strings.join(Arrays.copyOfRange(line, 1, line.length), " ");
                    Material newMat = mtllib.getMaterial(mat);
                    if (!Objects.equals(newMat, currentMat)) {
                        currentMat = newMat;
                        if (currentMesh != null && currentMesh.mat == null && currentMesh.faces.isEmpty()) {
                            currentMesh.mat = currentMat;
                        } else {
                            // Start new mesh
                            currentMesh = null;
                        }
                    }
                    break;

                case "v": // Vertex
                    model.positions.add(parseVector4To3(line));
                    break;
                case "vt": // Vertex texcoord
                    model.texCoords.add(parseUv(line, flipV));
                    break;
                case "vn": // Vertex normal
                    model.normals.add(parseVector3(line));
                    break;
                case "vc": // Vertex color (non-standard)
                    model.colors.add(parseVector4(line));
                    break;

                case "f": // Face
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
                        int[] vertex = Arrays.stream(vertexParts)
                            .mapToInt(num -> Strings.isNullOrEmpty(num) ? 0 : Integer.parseInt(num)).toArray();
                        if (vertex[0] < 0) {
                            vertex[0] = model.positions.size() + vertex[0];
                        } else {
                            vertex[0]--;
                        }
                        if (vertex.length > 1) {
                            if (vertex[1] < 0) {
                                vertex[1] = model.texCoords.size() + vertex[1];
                            } else {
                                vertex[1]--;
                            }
                            if (vertex.length > 2) {
                                if (vertex[2] < 0) {
                                    vertex[2] = model.normals.size() + vertex[2];
                                } else {
                                    vertex[2]--;
                                }
                                if (vertex.length > 3) {
                                    if (vertex[3] < 0) {
                                        vertex[3] = model.colors.size() + vertex[3];
                                    } else {
                                        vertex[3]--;
                                    }
                                }
                            }
                        }
                        vertices[i] = vertex;
                    }

                    currentMesh.faces.add(vertices);

                    break;

                case "s": // Smoothing group (starts new mesh)
                    String smoothingGroup = "off".equals(line[1]) ? null : line[1];
                    if (!Objects.equals(currentSmoothingGroup, smoothingGroup)) {
                        currentSmoothingGroup = smoothingGroup;
                        if (currentMesh != null && currentMesh.smoothingGroup == null && currentMesh.faces.isEmpty()) {
                            currentMesh.smoothingGroup = currentSmoothingGroup;
                        } else {
                            // Start new mesh
                            currentMesh = null;
                        }
                    }
                    break;

                case "g": {
                    String name = line[1];
                    if (objAboveGroup) {
                        currentObject = model.new ModelObject(currentGroup.name(), name);
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

                case "o":
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
        return model;
    }

    private static Vector3f parseVector4To3(String[] line) {
        Vector4f vec4 = parseVector4(line);
        return new Vector3f(vec4.x() / vec4.w(), vec4.y() / vec4.w(), vec4.z() / vec4.w());
    }

    private static Vec2 parseUv(String[] line, boolean flipV) {
        return switch (line.length) {
            case 1 -> new Vec2(0, 0);
            case 2 -> new Vec2(Float.parseFloat(line[1]), flipV ? 1 : 0);
            default ->
                new Vec2(Float.parseFloat(line[1]), flipV ? 1 - Float.parseFloat(line[2]) : Float.parseFloat(line[2]));
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
            default -> new Vector4f(
                Float.parseFloat(line[1]),
                Float.parseFloat(line[2]),
                Float.parseFloat(line[3]),
                Float.parseFloat(line[4])
            );
        };
    }

    @Override
    public QuadCollection bake(
        TextureSlots textureSlots,
        ModelBaker baker,
        ModelState state,
        ModelDebugName debugName
    ) {
        ContextMap.Builder propertiesBuilder = new ContextMap.Builder();
        NeoForgeModelProperties.fillRootTransformProperty(propertiesBuilder, parameters.rootTransform());
        NeoForgeModelProperties.fillPartVisibilityProperty(propertiesBuilder, parameters.partVisibility());
        return bake(textureSlots, baker, state, debugName, propertiesBuilder.create(NeoForgeModelProperties.TYPE));
    }

    @Override
    public QuadCollection bake(
        TextureSlots textureSlots,
        ModelBaker baker,
        ModelState state,
        ModelDebugName debugName,
        ContextMap additionalProperties
    ) {
        Map<String, Boolean> partVisibility = additionalProperties.getOrDefault(
            NeoForgeModelProperties.PART_VISIBILITY,
            Map.of()
        );
        var builder = new QuadCollection.Builder();
        parts.values().stream().filter(part -> partVisibility.getOrDefault(part.name(), true))
            .forEach(part -> part.addQuads(builder, textureSlots, baker, state, debugName, additionalProperties));
        return builder.build();
    }

    private Transparency computeMaterialTransparency(Baked material, TextureAtlasSprite texture, Vec2[] tex) {
        if (material.forceTranslucent()) {
            return Transparency.TRANSLUCENT;
        }
        Transparency transparency = texture.transparency();
        if (transparency.isOpaque()) {
            return transparency;
        }
        Vec2 t0 = tex[0], t1 = tex[1], t2 = tex[2], t3 = tex[3];
        return texture.contents().computeTransparency(
            Math.min(Math.min(t0.x, t1.x), Math.min(t2.x, t3.x)),
            Math.min(Math.min(t0.y, t1.y), Math.min(t2.y, t3.y)),
            Math.max(Math.max(t0.x, t1.x), Math.max(t2.x, t3.x)),
            Math.max(Math.max(t0.y, t1.y), Math.max(t2.y, t3.y))
        );
    }

    private long packUv(TextureAtlasSprite texture, Vec2 uv) {
        return UVPair.pack(texture.getU(uv.x), texture.getV(uv.y));
    }

    private Pair<BakedQuad, @Nullable Direction> makeQuad(
        Interner interner,
        int[][] indices,
        int tintIndex,
        Vector4f ambientColor,
        Baked material,
        Transformation transform,
        @Nullable Direction cull
    ) {
        boolean needsNormalRecalculation = false;
        Vector3f faceNormal = new Vector3f();
        for (int[] ints : indices) {
            if (ints.length < 3) {
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
                break;
            }
        }
        TextureAtlasSprite texture = material.sprite();
        Vector3f[] pos = new Vector3f[4];
        Vector3f norm;
        Vec2[] tex = new Vec2[4];
        if (transform.equals(Transformation.IDENTITY)) {
            int[] index = indices[Math.min(0, indices.length - 1)];
            pos[0] = positions.get(index[0]);
            tex[0] = index.length >= 2 && !texCoords.isEmpty() ? texCoords.get(index[1]) : DEFAULT_COORDS[0];
            norm = !needsNormalRecalculation && index.length >= 3 && !normals.isEmpty() ? normals.get(index[2]) :
                faceNormal;
            for (int i = 1; i < 4; i++) {
                index = indices[Math.min(i, indices.length - 1)];
                pos[i] = positions.get(index[0]);
                tex[i] = index.length >= 2 && !texCoords.isEmpty() ? texCoords.get(index[1]) : DEFAULT_COORDS[i];
            }
        } else {
            Matrix4fc matrix = transform.getMatrixCopy().translateLocal(0.5f, 0.5f, 0.5f)
                .translate(-0.5f, -0.5f, -0.5f);
            int[] index = indices[Math.min(0, indices.length - 1)];
            Vector4f position = new Vector4f(positions.get(index[0]), 1).mul(matrix);
            pos[0] = new Vector3f(position.x(), position.y(), position.z());
            norm = !needsNormalRecalculation && index.length >= 3 && !normals.isEmpty() ? normals.get(index[2]) :
                faceNormal;
            norm = new Vector3f(norm).mul(new Matrix3f(matrix).invert().transpose()).normalize();
            tex[0] = index.length >= 2 && !texCoords.isEmpty() ? texCoords.get(index[1]) : DEFAULT_COORDS[0];
            for (int i = 1; i < 4; i++) {
                index = indices[Math.min(i, indices.length - 1)];
                position = new Vector4f(positions.get(index[0]), 1).mul(matrix);
                pos[i] = new Vector3f(position.x(), position.y(), position.z());
                tex[i] = index.length >= 2 && !texCoords.isEmpty() ? texCoords.get(index[1]) : DEFAULT_COORDS[i];
            }
            if (cull != null) {
                cull = Direction.rotate(matrix, cull);
            }
        }
        int lightEmission;
        boolean shade;
        if (emissiveAmbient) {
            lightEmission = (int) ((ambientColor.x() + ambientColor.y() + ambientColor.z()) * 15 / 3.0f);
            shade = lightEmission == 0 && shadeQuads;
        } else {
            lightEmission = 0;
            shade = shadeQuads;
        }
        MaterialInfo materialInfo = interner.materialInfo(MaterialInfo.of(
            material,
            computeMaterialTransparency(material, texture, tex),
            tintIndex,
            shade,
            lightEmission
        ));
        float nx = norm.x(), ny = norm.y(), nz = norm.z();
        Direction direction = Direction.getApproximateNearest(nx, ny, nz);
        BakedQuad quad = new BakedQuad(
            pos[0],
            pos[1],
            pos[2],
            pos[3],
            packUv(texture, tex[0]),
            packUv(texture, tex[1]),
            packUv(texture, tex[2]),
            packUv(texture, tex[3]),
            direction,
            materialInfo
        );
        Vector3fc vec = direction.getUnitVec3f();
        if (!Mth.equal(nx, vec.x()) || !Mth.equal(ny, vec.y()) || !Mth.equal(nz, vec.z())) {
            BakedModelHelper.setNormals(quad, norm);
        }
        if (automaticCulling && cull == null) {
            if (Mth.equal(pos[0].x(), 0) && // vertex.position.x
                Mth.equal(pos[1].x(), 0) && Mth.equal(pos[2].x(), 0) && Mth.equal(
                pos[3].x(),
                0
            ) && nx < 0) // vertex.normal.x
            {
                cull = Direction.WEST;
            } else if (Mth.equal(pos[0].x(), 1) && // vertex.position.x
                Mth.equal(pos[1].x(), 1) && Mth.equal(pos[2].x(), 1) && Mth.equal(
                pos[3].x(),
                1
            ) && nx > 0) // vertex.normal.x
            {
                cull = Direction.EAST;
            } else if (Mth.equal(pos[0].z(), 0) && // vertex.position.z
                Mth.equal(pos[1].z(), 0) && Mth.equal(pos[2].z(), 0) && Mth.equal(
                pos[3].z(),
                0
            ) && nz < 0) // vertex.normal.z
            {
                cull = Direction.NORTH; // can never remember
            } else if (Mth.equal(pos[0].z(), 1) && // vertex.position.z
                Mth.equal(pos[1].z(), 1) && Mth.equal(pos[2].z(), 1) && Mth.equal(
                pos[3].z(),
                1
            ) && nz > 0) // vertex.normal.z
            {
                cull = Direction.SOUTH;
            } else if (Mth.equal(pos[0].y(), 0) && // vertex.position.y
                Mth.equal(pos[1].y(), 0) && Mth.equal(pos[2].y(), 0) && Mth.equal(
                pos[3].y(),
                0
            ) && ny < 0) // vertex.normal.z
            {
                cull = Direction.DOWN; // can never remember
            } else if (Mth.equal(pos[0].y(), 1) && // vertex.position.y
                Mth.equal(pos[1].y(), 1) && Mth.equal(pos[2].y(), 1) && Mth.equal(
                pos[3].y(),
                1
            ) && ny > 0) // vertex.normal.y
            {
                cull = Direction.UP;
            }
        }
        return Pair.of(quad, cull);
    }

    public class ModelObject {
        public final String name;
        public final @Nullable Direction cull;

        List<ModelMesh> meshes = Lists.newArrayList();

        ModelObject(String name) {
            this.name = name;
            cull = null;
        }

        ModelObject(String groupName, String name) {
            this.name = groupName + "/" + name;
            cull = Direction.byName(name);
        }

        public String name() {
            return name;
        }

        public void addQuads(
            QuadCollection.Builder builder,
            TextureSlots slots,
            ModelBaker baker,
            ModelState state,
            ModelDebugName debugName,
            ContextMap additionalProperties
        ) {
            for (ModelMesh mesh : meshes) {
                mesh.addQuads(builder, slots, baker, state, debugName, additionalProperties, cull);
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
            QuadCollection.Builder builder,
            TextureSlots slots,
            ModelBaker baker,
            ModelState state,
            ModelDebugName debugName,
            ContextMap additionalProperties
        ) {
            super.addQuads(builder, slots, baker, state, debugName, additionalProperties);

            Map<String, Boolean> partVisibility = additionalProperties.getOrDefault(
                NeoForgeModelProperties.PART_VISIBILITY,
                Map.of()
            );
            parts.values().stream()
                .filter(part -> partVisibility.getOrDefault("%s.%s".formatted(name(), part.name()), true))
                .forEach(part -> part.addQuads(builder, slots, baker, state, debugName, additionalProperties));
        }

        @Override
        protected void addNamesRecursively(Set<String> names) {
            super.addNamesRecursively(names);
            for (ModelObject object : parts.values()) {
                object.addNamesRecursively(names);
            }
        }
    }

    private class ModelMesh {
        @Nullable
        public Material mat;
        @Nullable
        public String smoothingGroup;
        public final List<int[][]> faces = Lists.newArrayList();

        public ModelMesh(@Nullable Material currentMat, @Nullable String currentSmoothingGroup) {
            mat = currentMat;
            smoothingGroup = currentSmoothingGroup;
        }

        public void addQuads(
            QuadCollection.Builder builder,
            TextureSlots slots,
            ModelBaker baker,
            ModelState state,
            ModelDebugName debugName,
            ContextMap additionalProperties,
            @Nullable Direction cull
        ) {
            if (mat == null) {
                return;
            }
            Baked texture = baker.materials().resolveSlot(slots, mat.diffuseColorMap, debugName);
            int tintIndex = mat.diffuseTintIndex;
            Vector4f ambientColor = mat.ambientColor;

            var rootTransform = additionalProperties.getOrDefault(
                NeoForgeModelProperties.TRANSFORM,
                Transformation.IDENTITY
            );
            var transform = rootTransform.equals(Transformation.IDENTITY) ? state.transformation() :
                state.transformation().compose(rootTransform);
            Interner interner = baker.interner();
            for (int[][] face : faces) {
                Pair<BakedQuad, @Nullable Direction> quad = makeQuad(
                    interner,
                    face,
                    tintIndex,
                    ambientColor,
                    texture,
                    transform,
                    cull
                );
                if (quad.getRight() == null) {
                    builder.addUnculledFace(quad.getLeft());
                } else {
                    builder.addCulledFace(quad.getRight(), quad.getLeft());
                }
            }
        }
    }

    public record Settings(Identifier modelLocation, boolean automaticCulling, boolean shadeQuads, boolean flipV,
                           boolean emissiveAmbient, @Nullable String mtlOverride, StandardModelParameters parameters) {
    }
}

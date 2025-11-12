package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.*;
import com.zurrtum.create.client.flywheel.api.model.Mesh;
import com.zurrtum.create.client.flywheel.lib.memory.MemoryBlock;
import com.zurrtum.create.client.flywheel.lib.model.SimpleQuadMesh;
import com.zurrtum.create.client.flywheel.lib.vertex.FullVertexView;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.function.BiConsumer;

public class ItemMeshEmitter implements VertexConsumer {
    private final RenderType renderType;
    private final ByteBufferBuilder byteBufferBuilder;
    @UnknownNullability
    private BufferBuilder bufferBuilder;

    private BakedItemModelBufferer.ResultConsumer resultConsumer;
    private BiConsumer<RenderType, Mesh> meshResultConsumer;
    private boolean currentShade;
    private boolean ended = true;

    ItemMeshEmitter(RenderType renderType) {
        this.renderType = renderType;
        this.byteBufferBuilder = new ByteBufferBuilder(renderType.bufferSize());
    }

    public void prepare(BakedItemModelBufferer.ResultConsumer resultConsumer, BiConsumer<RenderType, Mesh> meshResultConsumer) {
        this.resultConsumer = resultConsumer;
        this.meshResultConsumer = meshResultConsumer;
        ended = false;
    }

    public boolean isEnd() {
        return ended;
    }

    public void end() {
        if (ended) {
            return;
        }
        if (bufferBuilder != null) {
            emit();
        }
        resultConsumer = null;
        meshResultConsumer = null;
        ended = true;
    }

    public BufferBuilder unwrap(boolean shade) {
        prepareForGeometry(shade);
        return bufferBuilder;
    }

    private void prepareForGeometry(boolean shade) {
        if (bufferBuilder == null) {
            bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
        } else if (shade != currentShade) {
            emit();
            bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
        }

        currentShade = shade;
    }

    private void prepareForGeometry(BakedQuad quad) {
        prepareForGeometry(quad.shade());
    }

    private void emit() {
        var data = bufferBuilder.build();
        bufferBuilder = null;

        if (data != null) {
            resultConsumer.accept(renderType, currentShade, data);
            data.close();
        }
    }

    public void emit(
        ModelPart part,
        PoseStack stack,
        TextureAtlasSprite meshSprite,
        ItemMeshEmitter glintEmitter,
        int light,
        int overlay,
        int color
    ) {
        stack.pushPose();
        part.translateAndRotate(stack);
        if (!part.isEmpty()) {
            Mesh mesh = compile(part, stack, meshSprite, light, overlay, color);
            meshResultConsumer.accept(renderType, mesh);
            if (glintEmitter != null) {
                glintEmitter.meshResultConsumer.accept(glintEmitter.renderType, mesh);
            }
        }
        for (ModelPart child : part.children.values()) {
            emit(child, stack, meshSprite, glintEmitter, light, overlay, color);
        }
        stack.popPose();
    }

    private Mesh compile(ModelPart part, PoseStack stack, TextureAtlasSprite meshSprite, int light, int overlay, int color) {
        int vertexCount = 0;
        for (ModelPart.Cube cuboid : part.cubes) {
            vertexCount += cuboid.polygons.length * 4;
        }
        MemoryBlock memoryBlock = MemoryBlock.mallocTracked(vertexCount * FullVertexView.STRIDE);
        FullVertexView meshVertices = new FullVertexView();

        meshVertices.nativeMemoryOwner(memoryBlock);
        meshVertices.ptr(memoryBlock.ptr());
        meshVertices.vertexCount(vertexCount);

        PoseStack.Pose entry = stack.last();
        Matrix4f matrix4f = entry.pose();
        Vector3f vector3f = new Vector3f();
        int index = 0;
        float red = ARGB.redFloat(color);
        float green = ARGB.greenFloat(color);
        float blue = ARGB.blueFloat(color);
        float alpha = ARGB.alphaFloat(color);
        boolean hasUV = meshSprite != null;
        for (ModelPart.Cube cuboid : part.cubes) {
            for (ModelPart.Polygon quad : cuboid.polygons) {
                Vector3f normal = entry.transformNormal(quad.normal(), vector3f);
                float x = normal.x();
                float y = normal.y();
                float z = normal.z();
                for (ModelPart.Vertex vertex : quad.vertices()) {
                    float u = vertex.u();
                    float v = vertex.v();
                    if (hasUV) {
                        u = meshSprite.getU(u);
                        v = meshSprite.getV(v);
                    }
                    Vector3f position = matrix4f.transformPosition(vertex.x() / 16.0F, vertex.y() / 16.0F, vertex.z() / 16.0F, vector3f);
                    meshVertices.x(index, position.x());
                    meshVertices.y(index, position.y());
                    meshVertices.z(index, position.z());
                    meshVertices.r(index, red);
                    meshVertices.g(index, green);
                    meshVertices.b(index, blue);
                    meshVertices.a(index, alpha);
                    meshVertices.u(index, u);
                    meshVertices.v(index, v);
                    meshVertices.overlay(index, overlay);
                    meshVertices.light(index, light);
                    meshVertices.normalX(index, x);
                    meshVertices.normalY(index, y);
                    meshVertices.normalZ(index, z);
                    index++;
                }
            }
        }

        return new SimpleQuadMesh(meshVertices, "source=ItemMeshEmitter");
    }

    public void quad(
        PoseStack.Pose pose,
        BakedQuad quad,
        float red,
        float green,
        float blue,
        float alpha,
        int light,
        int overlay,
        boolean readExistingColor
    ) {
        prepareForGeometry(quad);
        bufferBuilder.putBulkData(
            pose,
            quad,
            new float[]{1.0F, 1.0F, 1.0F, 1.0F},
            red,
            green,
            blue,
            alpha,
            new int[]{light, light, light, light},
            overlay,
            readExistingColor
        );
    }

    @Override
    public void putBulkData(
        PoseStack.Pose pose,
        BakedQuad quad,
        float red,
        float green,
        float blue,
        float alpha,
        int packedLight,
        int packedOverlay
    ) {
        prepareForGeometry(quad);
        bufferBuilder.putBulkData(pose, quad, red, green, blue, alpha, packedLight, packedOverlay);
    }

    @Override
    public void putBulkData(
        PoseStack.Pose pose,
        BakedQuad quad,
        float[] brightnesses,
        float red,
        float green,
        float blue,
        float alpha,
        int[] lights,
        int overlay,
        boolean readExistingColor
    ) {
        prepareForGeometry(quad);
        bufferBuilder.putBulkData(pose, quad, brightnesses, red, green, blue, alpha, lights, overlay, readExistingColor);
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }
}

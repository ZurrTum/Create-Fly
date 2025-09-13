package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.zurrtum.create.client.flywheel.api.model.Mesh;
import com.zurrtum.create.client.flywheel.lib.memory.MemoryBlock;
import com.zurrtum.create.client.flywheel.lib.model.SimpleQuadMesh;
import com.zurrtum.create.client.flywheel.lib.vertex.FullVertexView;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.function.BiConsumer;

public class ItemMeshEmitter implements VertexConsumer {
    private final RenderLayer renderType;
    private final BufferAllocator byteBufferBuilder;
    @UnknownNullability
    private BufferBuilder bufferBuilder;

    private BakedItemModelBufferer.ResultConsumer resultConsumer;
    private BiConsumer<RenderLayer, Mesh> meshResultConsumer;
    private boolean currentShade;
    private boolean ended = true;

    ItemMeshEmitter(RenderLayer renderType) {
        this.renderType = renderType;
        this.byteBufferBuilder = new BufferAllocator(renderType.getExpectedBufferSize());
    }

    public void prepare(BakedItemModelBufferer.ResultConsumer resultConsumer, BiConsumer<RenderLayer, Mesh> meshResultConsumer) {
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
            bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
        } else if (shade != currentShade) {
            emit();
            bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
        }

        currentShade = shade;
    }

    private void prepareForGeometry(BakedQuad quad) {
        prepareForGeometry(quad.shade());
    }

    private void emit() {
        var data = bufferBuilder.endNullable();
        bufferBuilder = null;

        if (data != null) {
            resultConsumer.accept(renderType, currentShade, data);
            data.close();
        }
    }

    public void emit(ModelPart part, MatrixStack stack, Sprite meshSprite, ItemMeshEmitter glintEmitter, int light, int overlay, int color) {
        stack.push();
        part.applyTransform(stack);
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
        stack.pop();
    }

    private Mesh compile(ModelPart part, MatrixStack stack, Sprite meshSprite, int light, int overlay, int color) {
        int vertexCount = 0;
        for (ModelPart.Cuboid cuboid : part.cuboids) {
            vertexCount += cuboid.sides.length * 4;
        }
        MemoryBlock memoryBlock = MemoryBlock.mallocTracked(vertexCount * FullVertexView.STRIDE);
        FullVertexView meshVertices = new FullVertexView();

        meshVertices.nativeMemoryOwner(memoryBlock);
        meshVertices.ptr(memoryBlock.ptr());
        meshVertices.vertexCount(vertexCount);

        MatrixStack.Entry entry = stack.peek();
        Matrix4f matrix4f = entry.getPositionMatrix();
        Vector3f vector3f = new Vector3f();
        int index = 0;
        float red = ColorHelper.getRedFloat(color);
        float green = ColorHelper.getGreenFloat(color);
        float blue = ColorHelper.getBlueFloat(color);
        float alpha = ColorHelper.getAlphaFloat(color);
        boolean hasUV = meshSprite != null;
        for (ModelPart.Cuboid cuboid : part.cuboids) {
            for (ModelPart.Quad quad : cuboid.sides) {
                Vector3f normal = entry.transformNormal(quad.direction(), vector3f);
                float x = normal.x();
                float y = normal.y();
                float z = normal.z();
                for (ModelPart.Vertex vertex : quad.vertices()) {
                    Vector3f pos = vertex.pos();
                    float u = vertex.u();
                    float v = vertex.v();
                    if (hasUV) {
                        u = meshSprite.getFrameU(u);
                        v = meshSprite.getFrameV(v);
                    }
                    Vector3f position = matrix4f.transformPosition(pos.x() / 16.0F, pos.y() / 16.0F, pos.z() / 16.0F, vector3f);
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
        MatrixStack.Entry pose,
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
        bufferBuilder.quad(
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
    public void quad(MatrixStack.Entry pose, BakedQuad quad, float red, float green, float blue, float alpha, int packedLight, int packedOverlay) {
        prepareForGeometry(quad);
        bufferBuilder.quad(pose, quad, red, green, blue, alpha, packedLight, packedOverlay);
    }

    @Override
    public void quad(
        MatrixStack.Entry pose,
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
        bufferBuilder.quad(pose, quad, brightnesses, red, green, blue, alpha, lights, overlay, readExistingColor);
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer light(int u, int v) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer normal(float normalX, float normalY, float normalZ) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }
}

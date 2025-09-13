package com.zurrtum.create.client.catnip.render;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unchecked")
public class DefaultSuperByteBuffer implements SuperByteBuffer {

    protected ByteBuffer template;
    protected int formatSize;

    // Vertex Position
    protected MatrixStack transforms;

    // Vertex Coloring
    protected boolean shouldColor;
    protected int r, g, b, a;
    protected boolean disableDiffuse;

    // Vertex Texture Coordinates
    @Nullable
    protected SpriteShiftFunc spriteShiftFunc;

    // Vertex Overlay Color
    protected boolean hasOverlay;
    protected int overlay = OverlayTexture.DEFAULT_UV;

    // Vertex Lighting
    protected boolean useWorldLight;
    @Nullable
    protected Matrix4f lightTransform;
    protected boolean hasCustomLight;
    protected int packedLightCoordinates;
    protected boolean hybridLight;

    // Vertex Normals
    protected boolean fullNormalTransform;

    // Temporary
    protected static final Long2IntMap WORLD_LIGHT_CACHE = new Long2IntOpenHashMap();

    private final ShiftOutput shiftOutput = new ShiftOutput();


    public DefaultSuperByteBuffer(BuiltBuffer data) {
        ByteBuffer rendered = data.getBuffer();
        BuiltBuffer.DrawParameters drawState = data.getDrawParameters();

        // Vanilla issue, endianness does not carry over into sliced buffers - fixed by forge only
        rendered.order(ByteOrder.nativeOrder());

        drawState.format().getVertexSize();
        formatSize = drawState.format().getVertexSize();
        int size = drawState.vertexCount() * formatSize;

        template = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
        template.order(rendered.order());
        template.limit(rendered.limit());
        template.put(rendered);

        transforms = new MatrixStack();
        transforms.push();
    }

    @Override
    public void renderInto(MatrixStack ms, VertexConsumer consumer) {
        if (isEmpty())
            return;

        Matrix4f modelMatrix = new Matrix4f(ms.peek().getPositionMatrix());
        Matrix4f localTransforms = transforms.peek().getPositionMatrix();
        modelMatrix.mul(localTransforms);

        Matrix3f normalMatrix;
        if (fullNormalTransform) {
            normalMatrix = new Matrix3f(ms.peek().getNormalMatrix());
            normalMatrix.mul(transforms.peek().getNormalMatrix());
        } else {
            normalMatrix = new Matrix3f(transforms.peek().getNormalMatrix());
        }

        for (int i = 0; i < vertexCount(); i++) {
            float x = getX(i);
            float y = getY(i);
            float z = getZ(i);

            float normalX = getNX(i);
            float normalY = getNY(i);
            float normalZ = getNZ(i);

            Vector4f pos = new Vector4f(x, y, z, 1F);
            Vector3f normal = new Vector3f(normalX, normalY, normalZ);
            Vector4f lightPos = new Vector4f(x, y, z, 1F);
            pos.mul(modelMatrix);
            normal.mul(normalMatrix);
            lightPos.mul(localTransforms);

            consumer.vertex(pos.x(), pos.y(), pos.z());

            byte r, g, b, a;
            if (shouldColor) {
                r = (byte) this.r;
                g = (byte) this.g;
                b = (byte) this.b;
                a = (byte) this.a;
            } else {
                r = getR(i);
                g = getG(i);
                b = getB(i);
                a = getA(i);
            }
            if (disableDiffuse) {
                consumer.color(r, g, b, a);
            } else {
                // missing flywheel's diffuse calc stuff
                consumer.color(r, g, b, a);
            }
            float u = getU(i);
            float v = getV(i);

            if (spriteShiftFunc != null) {
                spriteShiftFunc.shift(u, v, shiftOutput);
                u = shiftOutput.u;
                v = shiftOutput.v;
            }

            consumer.texture(u, v);

            int light;
            if (useWorldLight) {
                lightPos.set(((x - .5f) * 15 / 16f) + .5f, (y - .5f) * 15 / 16f + .5f, (z - .5f) * 15 / 16f + .5f, 1f);
                lightPos.mul(localTransforms);
                if (lightTransform != null) {
                    lightPos.mul(lightTransform);
                }

                light = getLight(MinecraftClient.getInstance().world, lightPos);
                if (hasCustomLight) {
                    light = SuperByteBuffer.maxLight(light, packedLightCoordinates);
                }
            } else if (hasCustomLight) {
                light = packedLightCoordinates;
            } else {
                light = getLight(i);
            }

            if (hybridLight) {
                consumer.light(SuperByteBuffer.maxLight(light, getLight(i)));
            } else {
                consumer.light(light);
            }

            consumer.normal(normal.x(), normal.y(), normal.z());
        }

        reset();

    }

    @Override
    public DefaultSuperByteBuffer reset() {
        while (!transforms.isEmpty())
            transforms.pop();

        transforms.push();

        shouldColor = false;
        r = 0;
        g = 0;
        b = 0;
        a = 0;
        disableDiffuse = false;
        spriteShiftFunc = null;
        hasOverlay = false;
        overlay = OverlayTexture.DEFAULT_UV;
        useWorldLight = false;
        lightTransform = null;
        hasCustomLight = false;
        packedLightCoordinates = 0;
        hybridLight = false;
        fullNormalTransform = false;

        WORLD_LIGHT_CACHE.clear();

        return this;
    }

    @Override
    public boolean isEmpty() {
        return template.limit() == 0;
    }

    @Override
    public MatrixStack getTransforms() {
        return transforms;
    }

    @Override
    public DefaultSuperByteBuffer translate(float x, float y, float z) {
        transforms.translate(x, y, z);
        return this;
    }

    @Override
    public DefaultSuperByteBuffer translate(double x, double y, double z) {
        transforms.translate(x, y, z);
        return this;
    }

    @Override
    public DefaultSuperByteBuffer scale(float factorX, float factorY, float factorZ) {
        transforms.scale(factorX, factorY, factorZ);

        return this;
    }

    @Override
    public DefaultSuperByteBuffer pushPose() {
        transforms.push();
        return this;
    }

    @Override
    public DefaultSuperByteBuffer popPose() {
        transforms.pop();
        return this;
    }

    @Override
    public DefaultSuperByteBuffer mulPose(Matrix4fc matrix4fc) {
        transforms.peek().getPositionMatrix().mul(matrix4fc);
        return this;
    }

    @Override
    public DefaultSuperByteBuffer mulNormal(Matrix3fc matrix3fc) {
        transforms.peek().getNormalMatrix().mul(matrix3fc);
        return this;
    }

    @Override
    public DefaultSuperByteBuffer transform(MatrixStack ms) {
        transforms.peek().getPositionMatrix().mul(ms.peek().getPositionMatrix());
        transforms.peek().getNormalMatrix().mul(ms.peek().getNormalMatrix());
        return this;
    }

    @Override
    public DefaultSuperByteBuffer color(int color) {
        shouldColor = true;
        r = ((color >> 16) & 0xFF);
        g = ((color >> 8) & 0xFF);
        b = (color & 0xFF);
        a = 255;
        return this;
    }

    @Override
    public DefaultSuperByteBuffer color(int r, int g, int b, int a) {
        shouldColor = true;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        return this;
    }

    @Override
    public DefaultSuperByteBuffer disableDiffuse() {
        disableDiffuse = true;
        return this;
    }

    @Override
    public DefaultSuperByteBuffer shiftUV(SpriteShiftEntry entry) {
        spriteShiftFunc = (u, v, output) -> output.accept(entry.getTargetU(u), entry.getTargetV(v));
        return this;
    }

    @Override
    public DefaultSuperByteBuffer shiftUVScrolling(SpriteShiftEntry entry, float scrollU, float scrollV) {
        spriteShiftFunc = (u, v, output) -> {
            float targetU = u - entry.getOriginal().getMinU() + entry.getTarget().getMinU() + scrollU;
            float targetV = v - entry.getOriginal().getMinV() + entry.getTarget().getMinV() + scrollV;
            output.accept(targetU, targetV);
        };
        return this;
    }

    @Override
    public DefaultSuperByteBuffer shiftUVtoSheet(SpriteShiftEntry entry, float uTarget, float vTarget, int sheetSize) {
        spriteShiftFunc = (u, v, output) -> {
            float targetU = entry.getTarget().getFrameU((SpriteShiftEntry.getUnInterpolatedU(entry.getOriginal(), u) / sheetSize) + uTarget);
            float targetV = entry.getTarget().getFrameV((SpriteShiftEntry.getUnInterpolatedV(entry.getOriginal(), v) / sheetSize) + vTarget);
            output.accept(targetU, targetV);
        };
        return this;
    }

    @Override
    public DefaultSuperByteBuffer overlay(int overlay) {
        hasOverlay = true;
        this.overlay = overlay;
        return this;
    }

    @Override
    public DefaultSuperByteBuffer useLevelLight(BlockRenderView level) {
        return this;
    }

    @Override
    public DefaultSuperByteBuffer useLevelLight(BlockRenderView level, Matrix4f lightTransform) {
        return this;
    }

    @Override
    public DefaultSuperByteBuffer light(int packedLight) {
        hasCustomLight = true;
        this.packedLightCoordinates = packedLight;
        return this;
    }

    //

    protected int vertexCount() {
        return template.limit() / formatSize;
    }

    protected int getBufferPosition(int vertexIndex) {
        return vertexIndex * formatSize;
    }

    protected float getX(int index) {
        return template.getFloat(getBufferPosition(index));
    }

    protected float getY(int index) {
        return template.getFloat(getBufferPosition(index) + 4);
    }

    protected float getZ(int index) {
        return template.getFloat(getBufferPosition(index) + 8);
    }

    protected byte getR(int index) {
        return template.get(getBufferPosition(index) + 12);
    }

    protected byte getG(int index) {
        return template.get(getBufferPosition(index) + 13);
    }

    protected byte getB(int index) {
        return template.get(getBufferPosition(index) + 14);
    }

    protected byte getA(int index) {
        return template.get(getBufferPosition(index) + 15);
    }

    protected float getU(int index) {
        return template.getFloat(getBufferPosition(index) + 16);
    }

    protected float getV(int index) {
        return template.getFloat(getBufferPosition(index) + 20);
    }

    protected int getLight(int index) {
        return template.getInt(getBufferPosition(index) + 24);
    }

    protected byte getNX(int index) {
        return template.get(getBufferPosition(index) + 28);
    }

    protected byte getNY(int index) {
        return template.get(getBufferPosition(index) + 29);
    }

    protected byte getNZ(int index) {
        return template.get(getBufferPosition(index) + 30);
    }

    private static int getLight(World world, Vector4f lightPos) {
        BlockPos pos = BlockPos.ofFloored(lightPos.x(), lightPos.y(), lightPos.z());
        return WORLD_LIGHT_CACHE.computeIfAbsent(pos.asLong(), $ -> WorldRenderer.getLightmapCoordinates(world, pos));
    }

    @Override
    public SuperByteBuffer rotate(Quaternionfc quaternionfc) {
        return null;
    }
}

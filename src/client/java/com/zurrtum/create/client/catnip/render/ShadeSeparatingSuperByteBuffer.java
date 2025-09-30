package com.zurrtum.create.client.catnip.render;

import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.flywheel.backend.engine.uniform.LevelUniforms;
import com.zurrtum.create.client.flywheel.lib.util.ShadersModHelper;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.lang.Math;

public class ShadeSeparatingSuperByteBuffer implements SuperByteBuffer {
    private static final Long2IntMap WORLD_LIGHT_CACHE = new Long2IntOpenHashMap();

    private final TemplateMesh template;
    private final int[] shadeSwapVertices;

    // Vertex Position and Normals
    private final MatrixStack transforms = new MatrixStack();

    // Vertex Coloring
    private float r, g, b, a;
    private boolean disableDiffuse;

    // Vertex Texture Coords
    @Nullable
    private SpriteShiftFunc spriteShiftFunc;

    // Vertex Overlay
    private boolean hasCustomOverlay;
    private int overlay;

    // Vertex Light
    private boolean hasCustomLight;
    private int packedLight;
    private boolean useLevelLight;
    @Nullable
    private BlockRenderView levelWithLight;
    @Nullable
    private Matrix4f lightTransform;
    private boolean invertFakeDiffuseNormal;

    // Reused objects
    private final Matrix4f modelMat = new Matrix4f();
    private final Matrix3f normalMat = new Matrix3f();
    private final Vector4f pos = new Vector4f();
    private final Vector3f normal = new Vector3f();
    private final Vector3f lightDir0 = new Vector3f();
    private final Vector3f lightDir1 = new Vector3f();
    private final ShiftOutput shiftOutput = new ShiftOutput();
    private final Vector4f lightPos = new Vector4f();

    public ShadeSeparatingSuperByteBuffer(TemplateMesh template, int[] shadeSwapVertices, boolean invertFakeDiffuseNormal) {
        this.template = template;
        this.shadeSwapVertices = shadeSwapVertices;
        this.invertFakeDiffuseNormal = invertFakeDiffuseNormal;
        reset();
    }

    public ShadeSeparatingSuperByteBuffer(TemplateMesh template, int[] shadeSwapVertices) {
        this(template, shadeSwapVertices, false);
    }

    public ShadeSeparatingSuperByteBuffer(TemplateMesh template) {
        this(template, new int[0]);
    }

    public void renderInto(MatrixStack input, VertexConsumer builder) {
        if (isEmpty()) {
            return;
        }

        if (useLevelLight) {
            WORLD_LIGHT_CACHE.clear();
        }

        Matrix4f modelMat = this.modelMat.set(input.peek().getPositionMatrix());
        Matrix4f localTransforms = transforms.peek().getPositionMatrix();
        modelMat.mul(localTransforms);

        Matrix3f normalMat = this.normalMat.set(input.peek().getNormalMatrix());
        Matrix3f localNormalTransforms = transforms.peek().getNormalMatrix();
        normalMat.mul(localNormalTransforms);

        Vector4f pos = this.pos;
        Vector3f normal = this.normal;
        ShiftOutput shiftOutput = this.shiftOutput;
        Vector3f lightDir0 = this.lightDir0;
        Vector3f lightDir1 = this.lightDir1;
        Vector4f lightPos = this.lightPos;

        boolean applyDiffuse = !disableDiffuse && !ShadersModHelper.isShaderPackInUse();
        boolean shaded = true;
        int shadeSwapIndex = 0;
        int nextShadeSwapVertex = shadeSwapIndex < shadeSwapVertices.length ? shadeSwapVertices[shadeSwapIndex] : -1;
        float unshadedDiffuse = 1;
        if (applyDiffuse) {
            float[] currentLight = LevelUniforms.LIGHT_DIRECTION;
            lightDir0.set(currentLight[0], currentLight[1], currentLight[2]);
            lightDir1.set(currentLight[3], currentLight[4], currentLight[5]);
            if (shadeSwapVertices.length > 0) {
                // Pretend unshaded faces always point up to get the correct max diffuse value for the current level.
                normal.set(0, invertFakeDiffuseNormal ? -1 : 1, 0);
                // Don't apply the normal matrix since that would cause upside down objects to be dark.
                //                        unshadedDiffuse = calculateDiffuse(normal, lightDir0, lightDir1);
            }
        }

        int vertexCount = template.vertexCount();
        for (int i = 0; i < vertexCount; i++) {
            if (i == nextShadeSwapVertex) {
                shaded = !shaded;
                shadeSwapIndex++;
                nextShadeSwapVertex = shadeSwapIndex < shadeSwapVertices.length ? shadeSwapVertices[shadeSwapIndex] : -1;
            }

            float x = template.x(i);
            float y = template.y(i);
            float z = template.z(i);
            pos.set(x, y, z, 1.0f);
            pos.mul(modelMat);

            int packedNormal = template.normal(i);
            float normalX = ((byte) (packedNormal & 0xFF)) / 127.0f;
            float normalY = ((byte) ((packedNormal >>> 8) & 0xFF)) / 127.0f;
            float normalZ = ((byte) ((packedNormal >>> 16) & 0xFF)) / 127.0f;
            normal.set(normalX, normalY, normalZ);
            normal.mul(normalMat);

            int color = template.color(i);
            float r = (color & 0xFF) / 255.0f * this.r;
            float g = ((color >>> 8) & 0xFF) / 255.0f * this.g;
            float b = ((color >>> 16) & 0xFF) / 255.0f * this.b;
            float a = ((color >>> 24) & 0xFF) / 255.0f * this.a;
            if (applyDiffuse) {
                float diffuse = shaded ? calculateDiffuse(normal, lightDir0, lightDir1) : unshadedDiffuse;
                r *= diffuse;
                g *= diffuse;
                b *= diffuse;
            }

            float u = template.u(i);
            float v = template.v(i);
            if (spriteShiftFunc != null) {
                spriteShiftFunc.shift(u, v, shiftOutput);
                u = shiftOutput.u;
                v = shiftOutput.v;
            }

            int overlay;
            if (hasCustomOverlay) {
                overlay = this.overlay;
            } else {
                overlay = template.overlay(i);
            }

            int light = template.light(i);
            if (hasCustomLight) {
                light = SuperByteBuffer.maxLight(light, packedLight);
            }
            if (useLevelLight) {
                lightPos.set(((x - .5f) * 15 / 16f) + .5f, (y - .5f) * 15 / 16f + .5f, (z - .5f) * 15 / 16f + .5f, 1f);
                lightPos.mul(localTransforms);
                if (lightTransform != null) {
                    lightPos.mul(lightTransform);
                }
                light = SuperByteBuffer.maxLight(light, getLight(levelWithLight, lightPos));
            }

            builder.vertex(pos.x(), pos.y(), pos.z()).color(r, g, b, a).texture(u, v).overlay(overlay).light(light)
                .normal(normal.x(), normal.y(), normal.z());
        }

        reset();
    }

    public SuperByteBuffer reset() {
        while (!transforms.isEmpty())
            transforms.pop();
        transforms.push();

        r = 1;
        g = 1;
        b = 1;
        a = 1;
        disableDiffuse = false;
        spriteShiftFunc = null;
        hasCustomOverlay = false;
        overlay = OverlayTexture.DEFAULT_UV;
        hasCustomLight = false;
        packedLight = 0;
        useLevelLight = false;
        levelWithLight = null;
        lightTransform = null;
        return this;
    }

    public boolean isEmpty() {
        return template.isEmpty();
    }

    public MatrixStack getTransforms() {
        return transforms;
    }

    @Override
    public SuperByteBuffer scale(float factorX, float factorY, float factorZ) {
        transforms.scale(factorX, factorY, factorZ);
        return this;
    }

    @Override
    public SuperByteBuffer rotate(Quaternionfc quaternion) {
        var last = transforms.peek();
        last.getPositionMatrix().rotate(quaternion);
        last.getNormalMatrix().rotate(quaternion);
        return this;
    }

    @Override
    public SuperByteBuffer translate(float x, float y, float z) {
        transforms.translate(x, y, z);
        return this;
    }

    @Override
    public SuperByteBuffer mulPose(Matrix4fc pose) {
        transforms.peek().getPositionMatrix().mul(pose);
        return this;
    }

    @Override
    public SuperByteBuffer mulNormal(Matrix3fc normal) {
        transforms.peek().getNormalMatrix().mul(normal);
        return this;
    }

    @Override
    public SuperByteBuffer pushPose() {
        transforms.push();
        return this;
    }

    @Override
    public SuperByteBuffer popPose() {
        transforms.pop();
        return this;
    }

    public SuperByteBuffer color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        return this;
    }

    public SuperByteBuffer color(int r, int g, int b, int a) {
        color(r / 255.0f, g / 255.0f, b / 255.0f, a / 255.0f);
        return this;
    }

    public SuperByteBuffer color(int color) {
        color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, 255);
        return this;
    }

    public SuperByteBuffer color(Color c) {
        return color(c.getRGB());
    }

    public SuperByteBuffer disableDiffuse() {
        disableDiffuse = true;
        return this;
    }

    public SuperByteBuffer shiftUV(SpriteShiftEntry entry) {
        spriteShiftFunc = (u, v, output) -> {
            output.accept(entry.getTargetU(u), entry.getTargetV(v));
        };
        return this;
    }

    public SuperByteBuffer shiftUVScrolling(SpriteShiftEntry entry, float scrollV) {
        return shiftUVScrolling(entry, 0, scrollV);
    }

    public SuperByteBuffer shiftUVScrolling(SpriteShiftEntry entry, float scrollU, float scrollV) {
        spriteShiftFunc = (u, v, output) -> {
            float targetU = u - entry.getOriginal().getMinU() + entry.getTarget().getMinU() + scrollU;
            float targetV = v - entry.getOriginal().getMinV() + entry.getTarget().getMinV() + scrollV;
            output.accept(targetU, targetV);
        };
        return this;
    }

    public SuperByteBuffer shiftUVtoSheet(SpriteShiftEntry entry, float uTarget, float vTarget, int sheetSize) {
        spriteShiftFunc = (u, v, output) -> {
            float targetU = entry.getTarget().getFrameU((SpriteShiftEntry.getUnInterpolatedU(entry.getOriginal(), u) / sheetSize) + uTarget);
            float targetV = entry.getTarget().getFrameV((SpriteShiftEntry.getUnInterpolatedV(entry.getOriginal(), v) / sheetSize) + vTarget);
            output.accept(targetU, targetV);
        };
        return this;
    }

    public SuperByteBuffer overlay(int overlay) {
        hasCustomOverlay = true;
        this.overlay = overlay;
        return this;
    }

    public SuperByteBuffer light(int packedLight) {
        hasCustomLight = true;
        this.packedLight = packedLight;
        return this;
    }

    @Override
    public SuperByteBuffer useLevelLight(BlockRenderView level) {
        useLevelLight = true;
        levelWithLight = level;
        return this;
    }

    @Override
    public SuperByteBuffer useLevelLight(BlockRenderView level, Matrix4f lightTransform) {
        useLevelLight = true;
        levelWithLight = level;
        this.lightTransform = lightTransform;
        return this;
    }

    // Adapted from minecraft:shaders/include/light.glsl
    private static float calculateDiffuse(Vector3fc normal, Vector3fc lightDir0, Vector3fc lightDir1) {
        float light0 = Math.max(0.0f, lightDir0.dot(normal));
        float light1 = Math.max(0.0f, lightDir1.dot(normal));
        return Math.min(1.0f, (light0 + light1) * 0.6f + 0.4f);
    }

    private static int getLight(BlockRenderView world, Vector4f lightPos) {
        BlockPos pos = BlockPos.ofFloored(lightPos.x(), lightPos.y(), lightPos.z());
        return WORLD_LIGHT_CACHE.computeIfAbsent(pos.asLong(), $ -> WorldRenderer.getLightmapCoordinates(world, pos));
    }
}

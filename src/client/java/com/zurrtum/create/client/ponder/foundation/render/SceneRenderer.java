package com.zurrtum.create.client.ponder.foundation.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.client.catnip.gui.render.GpuTexture;
import com.zurrtum.create.client.catnip.render.PonderRenderTypes;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.PonderScene.SceneTransform;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector.CustomGeometryRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.feature.QuadParticleFeatureRenderer;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class SceneRenderer extends PictureInPictureRenderer<SceneRenderState> {
    private static final Vector3f DIFFUSE_LIGHT_0 = new Vector3f(0.4F, -1.0F, 0.7F).normalize();
    private static final Vector3f DIFFUSE_LIGHT_1 = new Vector3f(-0.4F, -0.5F, 0.7F).normalize();
    private static final Int2ObjectMap<GpuTexture> TEXTURES = new Int2ObjectArrayMap<>();
    private final PoseStack matrices = new PoseStack();
    private int windowScaleFactor;

    @Override
    public void prepare(
        SceneRenderState renderState,
        GuiRenderState state,
        FeatureRenderDispatcher featureRenderDispatcher,
        int windowScaleFactor
    ) {
        if (this.windowScaleFactor != windowScaleFactor) {
            this.windowScaleFactor = windowScaleFactor;
            TEXTURES.values().forEach(GpuTexture::close);
            TEXTURES.clear();
        }
        GpuTexture texture = TEXTURES.get(renderState.id());
        if (texture == null) {
            texture = GpuTexture.create(renderState.width(), renderState.height(), windowScaleFactor);
            TEXTURES.put(renderState.id(), texture);
        }
        texture.prepare(projection, projectionMatrixBuffer);
        matrices.pushPose();
        Minecraft mc = Minecraft.getInstance();
        GameRenderer gameRenderer = mc.gameRenderer;
        boolean lightOption = gameRenderer.useUiLightmap;
        gameRenderer.useUiLightmap = false;
        Lighting lighting = gameRenderer.lighting();
        lighting.updateBuffer(Lighting.Entry.LEVEL, DIFFUSE_LIGHT_0, DIFFUSE_LIGHT_1);
        lighting.setupFor(Lighting.Entry.LEVEL);
        PonderScene scene = renderState.scene();
        renderScene(
            mc,
            submitNodeStorage,
            matrices,
            scene,
            renderState.partialTicks(),
            renderState.width(),
            renderState.height(),
            renderState.slide(),
            renderState.finishingFlash()
        );
        DynamicTransformsHolder particle = (DynamicTransformsHolder) featureRenderDispatcher.featureRenderers.get(
            QuadParticleFeatureRenderer.TYPE);
        Matrix4f particleTransforms = RenderSystem.getModelViewMatrixCopy().mul(matrices.last().pose());
        particle.ponder$updateTransforms(RenderSystem.getDynamicUniforms().writeTransform(particleTransforms));
        texture.render(featureRenderDispatcher, submitNodeStorage);
        particle.ponder$updateTransforms(null);
        scene.resetParticles();
        lighting.updateLevel(mc.level.dimensionType().cardinalLightType());
        gameRenderer.useUiLightmap = lightOption;
        matrices.popPose();
        state.addBlitToCurrentLayer(new BlitRenderState(
            RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
            TextureSetup.singleTexture(
                texture.textureView(),
                RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST)
            ),
            renderState.pose(),
            renderState.x0(),
            renderState.y0(),
            renderState.x1(),
            renderState.y1(),
            0.0F,
            1.0F,
            1.0F,
            0.0F,
            -1,
            null,
            null
        ));
    }

    private static void renderScene(
        Minecraft mc,
        SubmitNodeCollector queue,
        PoseStack poseStack,
        PonderScene scene,
        float partialTicks,
        int width,
        int height,
        double slide,
        LerpedFloat finishingFlash
    ) {
        poseStack.translate(0, 0, -800);
        SceneTransform transform = scene.getTransform();
        transform.updateScreenParams(width, height, slide);
        transform.apply(poseStack, partialTicks);
        transform.updateSceneRVE(partialTicks);
        if (!scene.shouldHidePlatformShadow()) {
            poseStack.pushPose();
            Pose pose = poseStack.last();
            pose.scale(1, -1, 1);
            int basePlateSize = scene.getBasePlateSize();
            pose.translate(scene.getBasePlateOffsetX() + basePlateSize, 0, scene.getBasePlateOffsetZ());
            queue.submitCustomGeometry(
                poseStack,
                PonderRenderTypes.getGui(),
                new ShadowRenderState(
                    basePlateSize,
                    ShadowFlashRenderState.create(finishingFlash.getValue(partialTicks)),
                    new Vector3f()
                )
            );
            poseStack.popPose();
        }
        scene.renderScene(mc, queue, poseStack, partialTicks);

        //TODO
        // coords for debug
        //        if (PonderIndex.editingModeActive() && !state.userViewMode()) {
        //            BlockBox bounds = scene.getBounds();
        //
        //            poseStack.scale(-1, -1, 1);
        //            poseStack.scale(1 / 16f, 1 / 16f, 1 / 16f);
        //            poseStack.translate(1, -8, -1 / 64f);
        //
        //            // X AXIS
        //            poseStack.push();
        //            poseStack.translate(4, -3, 0);
        //            poseStack.translate(0, 0, -2 / 1024f);
        //            int blockCountX = bounds.getBlockCountX();
        //            for (int x = 0; x <= blockCountX; x++) {
        //                poseStack.translate(-16, 0, 0);
        //                graphics.drawString(font, x == blockCountX ? "x" : "" + x, 0, 0, 0xFFFFFFFF, false);
        //            }
        //            poseStack.pop();
        //
        //            // Z AXIS
        //            poseStack.push();
        //            poseStack.scale(-1, 1, 1);
        //            poseStack.translate(0, -3, -4);
        //            poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90));
        //            poseStack.translate(-8, -2, 2 / 64f);
        //            int blockCountZ = bounds.getBlockCountZ();
        //            for (int z = 0; z <= blockCountZ; z++) {
        //                poseStack.translate(16, 0, 0);
        //                graphics.drawString(font, z == blockCountZ ? "z" : "" + z, 0, 0, 0xFFFFFFFF, false);
        //            }
        //            poseStack.pop();
        //
        //            // DIRECTIONS
        //            poseStack.push();
        //            poseStack.translate(blockCountX * -8, 0, blockCountZ * 8);
        //            poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90));
        //            for (Direction d : Iterate.horizontalDirections) {
        //                poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
        //                poseStack.push();
        //                poseStack.translate(0, 0, blockCountZ * 16);
        //                poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));
        //                graphics.drawString(font, d.name().substring(0, 1), 0, 0, 0x66FFFFFF, false);
        //                graphics.drawString(font, "|", 2, 10, 0x44FFFFFF, false);
        //                graphics.drawString(font, ".", 2, 14, 0x22FFFFFF, false);
        //                poseStack.pop();
        //            }
        //            poseStack.pop();
        //            buffer.draw();
        //        }
    }

    public static void fillGradient(
        VertexConsumer buffer,
        PoseStack matrices,
        int x1,
        int y1,
        int x2,
        int y2,
        int z,
        int colorFrom,
        int colorTo
    ) {
        Matrix4f matrix4f = matrices.last().pose();
        buffer.addVertex(matrix4f, x1, y1, z).setColor(colorFrom);
        buffer.addVertex(matrix4f, x1, y2, z).setColor(colorTo);
        buffer.addVertex(matrix4f, x2, y2, z).setColor(colorTo);
        buffer.addVertex(matrix4f, x2, y1, z).setColor(colorFrom);
    }

    @Override
    protected void renderToTexture(
        SceneRenderState state,
        PoseStack matrices,
        SubmitNodeCollector submitNodeCollector
    ) {
    }

    @Override
    protected String getTextureLabel() {
        return "Scene";
    }

    @Override
    public Class<SceneRenderState> getRenderStateClass() {
        return SceneRenderState.class;
    }

    private record ShadowRenderState(float basePlateSize, @Nullable ShadowFlashRenderState flash,
                                     Vector3f pos) implements CustomGeometryRenderer {
        @Override
        public void render(Pose stack, VertexConsumer buffer) {
            Matrix4f pose = stack.pose();
            float x2 = -basePlateSize;
            if (flash == null) {
                addBlackVertex(buffer, pose, pos, x2);
                for (int i = 0; i < 3; i++) {
                    move(pose);
                    addBlackVertex(buffer, pose, pos, x2);
                }
            } else {
                addBlackVertex(buffer, pose, pos, x2);
                flash.addVertex(buffer, pose, pos, x2);
                for (int i = 0; i < 3; i++) {
                    move(pose);
                    addBlackVertex(buffer, pose, pos, x2);
                    flash.addVertex(buffer, pose, pos, x2);
                }
            }
        }

        private void move(Matrix4f pose) {
            pose.rotateY(Radians.DEG_270);
            pose.translate(basePlateSize, 0, 0);
        }

        private static void addBlackVertex(VertexConsumer buffer, Matrix4f pose, Vector3f pos, float x2) {
            addVertex(buffer, pose, pos, -0.5f, x2, 4.0f, 0x66_000000, 0x00_000000);
        }

        private static void addVertex(
            VertexConsumer buffer,
            Matrix4f pose,
            Vector3f pos,
            float y1,
            float x2,
            float y2,
            int colorFrom,
            int colorTo
        ) {
            addVertex(buffer, pose, pos, 0.0f, y1, colorFrom);
            addVertex(buffer, pose, pos, 0.0f, y2, colorTo);
            addVertex(buffer, pose, pos, x2, y2, colorTo);
            addVertex(buffer, pose, pos, x2, y1, colorFrom);
        }

        private static void addVertex(VertexConsumer buffer, Matrix4f pose, Vector3f pos, float x, float y, int color) {
            pos.set(x, y, 0).mulPosition(pose);
            buffer.addVertex(pos.x, pos.y, pos.z).setColor(color);
        }
    }

    private record ShadowFlashRenderState(int flashColor, float scaleY, Matrix4f save) {
        @Nullable
        public static ShadowFlashRenderState create(float progress) {
            float alpha = progress * 0.9f;
            progress = alpha * alpha;
            progress = 4 * progress * (1 - progress);
            if (progress <= 0) {
                return null;
            }
            return new ShadowFlashRenderState(
                0xc6ffc9 | (int) (0xaa * alpha) << 24,
                Math.fma(progress, 0.75f, 0.5f),
                new Matrix4f()
            );
        }

        public void addVertex(VertexConsumer buffer, Matrix4f pose, Vector3f pos, float x2) {
            save.set(pose);
            pose.translate(0, 0, -1 / 1024.0f);
            pose.scale(1, scaleY, 1);
            ShadowRenderState.addVertex(buffer, pose, pos, -1.0f, x2, 0, 0x00_c6ffc9, flashColor);
            pose.set(save);
        }
    }
}

package com.zurrtum.create.client.ponder.foundation.render;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.gui.UIRenderHelper;
import com.zurrtum.create.client.catnip.gui.render.GpuTexture;
import com.zurrtum.create.client.catnip.render.DefaultSuperRenderTypeBuffer;
import com.zurrtum.create.client.catnip.render.PonderRenderTypes;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.PonderScene.SceneTransform;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.BlitRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class SceneRenderer extends PictureInPictureRenderer<SceneRenderState> {
    private static final Vector3f DIFFUSE_LIGHT_0 = new Vector3f(0.4F, -1.0F, 0.7F).normalize();
    private static final Vector3f DIFFUSE_LIGHT_1 = new Vector3f(-0.4F, -0.5F, 0.7F).normalize();
    private static final Int2ObjectMap<GpuTexture> TEXTURES = new Int2ObjectArrayMap<>();
    private final PoseStack matrices = new PoseStack();
    private int windowScaleFactor;

    public SceneRenderer(MultiBufferSource.BufferSource vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    public void prepare(SceneRenderState renderState, GuiRenderState state, int windowScaleFactor) {
        if (this.windowScaleFactor != windowScaleFactor) {
            this.windowScaleFactor = windowScaleFactor;
            TEXTURES.values().forEach(GpuTexture::close);
            TEXTURES.clear();
        }
        int width = renderState.width() * windowScaleFactor;
        int height = renderState.height() * windowScaleFactor;
        GpuTexture texture = TEXTURES.get(renderState.id());
        if (texture == null) {
            texture = GpuTexture.create(width, height);
            TEXTURES.put(renderState.id(), texture);
        }
        RenderSystem.setProjectionMatrix(projectionMatrixBuffer.getBuffer(width, height), ProjectionType.ORTHOGRAPHIC);
        texture.prepare();
        matrices.pushPose();
        matrices.scale(windowScaleFactor, windowScaleFactor, 1);

        Minecraft mc = Minecraft.getInstance();
        GameRenderer gameRenderer = mc.gameRenderer;
        boolean lightOption = gameRenderer.useUiLightmap;
        gameRenderer.useUiLightmap = false;
        Lighting lighting = gameRenderer.getLighting();
        lighting.updateBuffer(Lighting.Entry.LEVEL, DIFFUSE_LIGHT_0, DIFFUSE_LIGHT_1);
        lighting.setupFor(Lighting.Entry.LEVEL);
        FeatureRenderDispatcher renderDispatcher = gameRenderer.getFeatureRenderDispatcher();
        renderScene(mc, renderState, matrices, renderDispatcher.getSubmitNodeStorage());
        renderDispatcher.renderAllFeatures();
        bufferSource.endBatch();
        lighting.updateLevel(mc.level.dimensionType().cardinalLightType());
        gameRenderer.useUiLightmap = lightOption;
        matrices.popPose();
        texture.clear();
        state.submitBlitToCurrentLayer(new BlitRenderState(
            RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
            TextureSetup.singleTexture(texture.textureView(), RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST)),
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

    private void renderScene(Minecraft mc, SceneRenderState state, PoseStack poseStack, SubmitNodeStorage queue) {
        float partialTicks = state.partialTicks();
        SuperRenderTypeBuffer buffer = DefaultSuperRenderTypeBuffer.getInstance();
        PonderScene scene = state.scene();
        poseStack.translate(0, 0, -800);
        SceneTransform transform = scene.getTransform();
        transform.updateScreenParams(state.width(), state.height(), state.slide());
        transform.apply(poseStack, partialTicks);
        transform.updateSceneRVE(partialTicks);

        // kool shadow fx
        if (!scene.shouldHidePlatformShadow()) {
            poseStack.pushPose();
            poseStack.translate(scene.getBasePlateOffsetX(), 0, scene.getBasePlateOffsetZ());
            UIRenderHelper.flipForGuiRender(poseStack);

            float flash = state.finishingFlash().getValue(partialTicks) * .9f;
            float alpha = flash;
            flash *= flash;
            flash = ((flash * 2) - 1);
            flash *= flash;
            flash = 1 - flash;

            for (int f = 0; f < 4; f++) {
                poseStack.translate(scene.getBasePlateSize(), 0, 0);
                poseStack.pushPose();
                poseStack.translate(0, 0, -1 / 1024f);
                if (flash > 0) {
                    poseStack.pushPose();
                    poseStack.scale(1, .5f + flash * .75f, 1);
                    fillGradient(
                        bufferSource,
                        matrices,
                        0,
                        -1,
                        -scene.getBasePlateSize(),
                        0,
                        0,
                        new Color(0x00_c6ffc9).getRGB(),
                        new Color(0xaa_c6ffc9).scaleAlpha(alpha).getRGB()
                    );
                    poseStack.popPose();
                }
                poseStack.translate(0, 0, 2 / 1024f);
                fillGradient(
                    bufferSource,
                    matrices,
                    0,
                    0,
                    -scene.getBasePlateSize(),
                    4,
                    0,
                    new Color(0x66_000000).getRGB(),
                    new Color(0x00_000000).getRGB()
                );
                poseStack.popPose();
                poseStack.mulPose(Axis.YP.rotationDegrees(-90));
            }
            poseStack.popPose();
        }
        bufferSource.endBatch();
        scene.renderScene(mc, buffer, queue, poseStack, partialTicks);
        buffer.draw();

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
        MultiBufferSource vertexConsumers,
        PoseStack matrices,
        int x1,
        int y1,
        int x2,
        int y2,
        int z,
        int colorFrom,
        int colorTo
    ) {
        VertexConsumer buffer = vertexConsumers.getBuffer(PonderRenderTypes.getGui());
        Matrix4f matrix4f = matrices.last().pose();
        buffer.addVertex(matrix4f, (float) x1, (float) y1, (float) z).setColor(colorFrom);
        buffer.addVertex(matrix4f, (float) x1, (float) y2, (float) z).setColor(colorTo);
        buffer.addVertex(matrix4f, (float) x2, (float) y2, (float) z).setColor(colorTo);
        buffer.addVertex(matrix4f, (float) x2, (float) y1, (float) z).setColor(colorFrom);
    }

    @Override
    protected void renderToTexture(SceneRenderState state, PoseStack matrices) {
    }

    @Override
    protected String getTextureLabel() {
        return "Scene";
    }

    @Override
    public Class<SceneRenderState> getRenderStateClass() {
        return SceneRenderState.class;
    }
}

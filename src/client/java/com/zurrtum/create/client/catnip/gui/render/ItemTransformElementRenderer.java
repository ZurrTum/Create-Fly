package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.BlitRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;

import java.util.HashMap;
import java.util.Map;

public class ItemTransformElementRenderer extends PictureInPictureRenderer<ItemTransformRenderState> {
    private static final Map<Object, GpuTexture> TEXTURES = new HashMap<>();
    private final PoseStack matrices = new PoseStack();
    private int windowScaleFactor;

    public ItemTransformElementRenderer(MultiBufferSource.BufferSource vertexConsumers) {
        super(vertexConsumers);
    }

    public static void clear(Object key) {
        GpuTexture texture = TEXTURES.remove(key);
        if (texture != null) {
            texture.close();
        }
    }

    @Override
    public void prepare(ItemTransformRenderState item, GuiRenderState state, int windowScaleFactor) {
        if (this.windowScaleFactor != windowScaleFactor) {
            this.windowScaleFactor = windowScaleFactor;
            TEXTURES.values().forEach(GpuTexture::close);
            TEXTURES.clear();
        }
        float size = item.scale() * windowScaleFactor;
        Object key = item.getKey();
        GpuTexture texture = TEXTURES.get(key);
        boolean draw;
        if (texture == null) {
            texture = GpuTexture.create((int) size);
            TEXTURES.put(key, texture);
            draw = true;
        } else {
            draw = item.state().isAnimated();
        }
        if (draw) {
            RenderSystem.setProjectionMatrix(projectionMatrixBuffer.getBuffer(size, size), ProjectionType.ORTHOGRAPHIC);
            texture.prepare();
            matrices.pushPose();
            matrices.translate(size / 2, size / 2, 0);
            if (item.padding() != 0) {
                size -= item.padding() * windowScaleFactor;
            }
            matrices.scale(size, -size, size);
            if (item.zRot() != 0) {
                matrices.mulPose(Axis.ZP.rotation(item.zRot()));
            }
            if (item.xRot() != 0) {
                matrices.mulPose(Axis.XP.rotation(item.xRot()));
            }
            if (item.yRot() != 0) {
                matrices.mulPose(Axis.YP.rotation(item.yRot()));
            }
            boolean blockLight = item.state().usesBlockLight();
            Lighting lighting = Minecraft.getInstance().gameRenderer.getLighting();
            if (blockLight) {
                lighting.setupFor(Lighting.Entry.ITEMS_3D);
            } else {
                lighting.setupFor(Lighting.Entry.ITEMS_FLAT);
            }
            FeatureRenderDispatcher renderDispatcher = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();
            SubmitNodeStorage queue = renderDispatcher.getSubmitNodeStorage();
            item.state().submit(matrices, queue, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
            renderDispatcher.renderAllFeatures();
            bufferSource.endBatch();
            matrices.popPose();
            texture.clear();
        }
        state.submitBlitToCurrentLayer(new BlitRenderState(
            RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
            TextureSetup.singleTexture(texture.textureView()),
            item.pose(),
            item.x0(),
            item.y0(),
            item.x1(),
            item.y1(),
            0.0F,
            1.0F,
            1.0F,
            0.0F,
            -1,
            item.scissorArea(),
            null
        ));
    }

    @Override
    protected void renderToTexture(ItemTransformRenderState item, PoseStack matrices) {
    }

    @Override
    protected String getTextureLabel() {
        return "Item Transform";
    }

    @Override
    public Class<ItemTransformRenderState> getRenderStateClass() {
        return ItemTransformRenderState.class;
    }
}

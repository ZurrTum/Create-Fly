package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
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

import java.util.IdentityHashMap;
import java.util.Map;

public class ItemTransformElementRenderer extends PictureInPictureRenderer<ItemTransformRenderState> {
    private static final Map<ItemTransformRenderKey, GpuTexture> TEXTURES = new IdentityHashMap<>();
    private final PoseStack matrices = new PoseStack();
    private int windowScaleFactor;

    public ItemTransformElementRenderer(MultiBufferSource.BufferSource vertexConsumers) {
        super(vertexConsumers);
    }

    public static void clear(ItemTransformRenderKey key) {
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
        float size = 0;
        ItemTransformRenderKey key = item.key();
        GpuTexture texture = TEXTURES.get(key);
        boolean draw;
        if (texture == null || key.dirty) {
            size = key.size * windowScaleFactor;
            if (key.dirty) {
                key.dirty = false;
                if (texture != null && texture.width() != size) {
                    texture.close();
                    texture = null;
                }
            }
            if (texture == null) {
                texture = GpuTexture.create((int) size);
                TEXTURES.put(key, texture);
            }
            draw = true;
        } else {
            draw = key.state.isAnimated();
        }
        if (draw) {
            if (size == 0) {
                size = key.size * windowScaleFactor;
            }
            texture.prepare(projectionMatrixBuffer);
            matrices.pushPose();
            matrices.translate(size / 2, size / 2, 0);
            if (key.padding != 0) {
                size -= key.padding * windowScaleFactor;
            }
            matrices.scale(size, -size, size);
            if (key.zRot != 0) {
                matrices.mulPose(Axis.ZP.rotation(key.zRot));
            }
            if (key.xRot != 0) {
                matrices.mulPose(Axis.XP.rotation(key.xRot));
            }
            if (key.yRot != 0) {
                matrices.mulPose(Axis.YP.rotation(key.yRot));
            }
            boolean blockLight = key.state.usesBlockLight();
            Lighting lighting = Minecraft.getInstance().gameRenderer.getLighting();
            if (blockLight) {
                lighting.setupFor(Lighting.Entry.ITEMS_3D);
            } else {
                lighting.setupFor(Lighting.Entry.ITEMS_FLAT);
            }
            FeatureRenderDispatcher renderDispatcher = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();
            SubmitNodeStorage queue = renderDispatcher.getSubmitNodeStorage();
            key.state.submit(matrices, queue, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
            renderDispatcher.renderAllFeatures();
            bufferSource.endBatch();
            matrices.popPose();
            texture.clear();
        }
        state.submitBlitToCurrentLayer(new BlitRenderState(
            RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
            TextureSetup.singleTexture(texture.textureView(),
                RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST)
            ),
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

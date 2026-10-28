package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;

import java.util.HashMap;
import java.util.Map;

public class BlockTransformElementRenderer extends PictureInPictureRenderer<BlockTransformRenderState> {
    private static final Map<Object, GpuTexture> TEXTURES = new HashMap<>();
    private final PoseStack matrices = new PoseStack();
    private int windowScaleFactor;

    public BlockTransformElementRenderer() {
    }

    public static void clear(Object key) {
        GpuTexture texture = TEXTURES.remove(key);
        if (texture != null) {
            texture.close();
        }
    }

    @Override
    public void prepare(
        BlockTransformRenderState block,
        GuiRenderState state,
        FeatureRenderDispatcher featureRenderDispatcher,
        int windowScaleFactor
    ) {
        if (this.windowScaleFactor != windowScaleFactor) {
            this.windowScaleFactor = windowScaleFactor;
            TEXTURES.values().forEach(GpuTexture::close);
            TEXTURES.clear();
        }
        BlockTransformRenderKey key = block.key();
        GpuTexture texture = TEXTURES.get(key);
        if (texture == null || key.dirty) {
            float size = key.size * windowScaleFactor;
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
            texture.prepare(projection, projectionMatrixBuffer);
            matrices.pushPose();
            Pose pose = matrices.last();
            pose.translate(size / 2, size / 2, 0);
            if (key.padding != 0) {
                size -= key.padding * windowScaleFactor;
            }
            pose.scale(size, size, size);
            if (key.zRadians != 0) {
                pose.rotate(Axis.ZP, key.zRadians);
            }
            if (key.xRadians != 0) {
                pose.rotate(Axis.XP, key.xRadians);
            }
            if (key.yRadians != 0) {
                pose.rotate(Axis.YP, key.yRadians);
            }
            pose.scale(1, -1, 1);
            pose.translate(-0.5F, -0.5F, -0.5F);
            CachedBuffers.block(key.state).submit(matrices, submitNodeStorage);
            matrices.popPose();
            featureRenderDispatcher.renderAllFeatures(submitNodeStorage);
            texture.clear();
        }
        state.addBlitToCurrentLayer(new BlitRenderState(
            RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
            TextureSetup.singleTexture(
                texture.textureView(),
                RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST)
            ),
            block.pose(),
            block.x0(),
            block.y0(),
            block.x1(),
            block.y1(),
            0.0F,
            1.0F,
            1.0F,
            0.0F,
            -1,
            block.scissorArea(),
            null
        ));
    }

    @Override
    protected void renderToTexture(
        BlockTransformRenderState block,
        PoseStack matrices,
        SubmitNodeCollector submitNodeCollector
    ) {
    }

    @Override
    protected String getTextureLabel() {
        return "Block Transform";
    }

    @Override
    public Class<BlockTransformRenderState> getRenderStateClass() {
        return BlockTransformRenderState.class;
    }
}

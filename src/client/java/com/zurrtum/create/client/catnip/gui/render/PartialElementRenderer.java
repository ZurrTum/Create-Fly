package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.world.level.block.Blocks;

import java.util.IdentityHashMap;
import java.util.Map;

public class PartialElementRenderer extends PictureInPictureRenderer<PartialRenderState> {
    private static final Map<PartialRenderState, GpuTexture> TEXTURES = new IdentityHashMap<>();
    private final PoseStack matrices = new PoseStack();
    private int windowScaleFactor;

    public static void clear(PartialRenderState block) {
        GpuTexture texture = TEXTURES.remove(block);
        if (texture != null) {
            texture.close();
        }
    }

    @Override
    public void prepare(
        PartialRenderState partial,
        GuiRenderState state,
        FeatureRenderDispatcher featureRenderDispatcher,
        int windowScaleFactor
    ) {
        if (partial.model == null) {
            return;
        }
        if (this.windowScaleFactor != windowScaleFactor) {
            this.windowScaleFactor = windowScaleFactor;
            TEXTURES.values().forEach(GpuTexture::close);
            TEXTURES.clear();
        }
        GpuTexture texture = TEXTURES.get(partial);
        boolean draw = texture == null || partial.dirty;
        if (draw) {
            float size = partial.size * windowScaleFactor;
            if (partial.dirty) {
                partial.clearDirty();
                if (texture != null && texture.width() != size) {
                    texture.close();
                    texture = null;
                }
            }
            if (texture == null) {
                texture = GpuTexture.create((int) size);
                TEXTURES.put(partial, texture);
            }
            texture.prepare(projection, projectionMatrixBuffer);
            matrices.pushPose();
            if (partial.padding != 0) {
                size -= partial.padding * windowScaleFactor;
            }
            matrices.scale(size, size, size);
            partial.transform(matrices);
            CachedBuffers.partial(partial.model, Blocks.AIR.defaultBlockState()).submit(matrices, submitNodeStorage);
            matrices.popPose();
            texture.render(featureRenderDispatcher, submitNodeStorage);
        }
        state.addBlitToCurrentLayer(new BlitRenderState(
            RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
            TextureSetup.singleTexture(
                texture.textureView(),
                RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST)
            ),
            partial.pose,
            partial.x1,
            partial.y1,
            partial.x2,
            partial.y2,
            0.0F,
            1.0F,
            1.0F,
            0.0F,
            -1,
            partial.scissor,
            null
        ));
    }

    @Override
    protected void renderToTexture(
        PartialRenderState partial,
        PoseStack matrices,
        SubmitNodeCollector submitNodeCollector
    ) {
    }

    @Override
    protected String getTextureLabel() {
        return "Partial";
    }

    @Override
    public Class<PartialRenderState> getRenderStateClass() {
        return PartialRenderState.class;
    }
}

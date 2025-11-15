package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import com.zurrtum.create.client.model.LayerBakedModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.BlitRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class PartialElementRenderer extends PictureInPictureRenderer<PartialRenderState> {
    private static final Map<PartialRenderState, GpuTexture> TEXTURES = new IdentityHashMap<>();
    private final PoseStack matrices = new PoseStack();
    private int windowScaleFactor;

    public PartialElementRenderer(MultiBufferSource.BufferSource vertexConsumers) {
        super(vertexConsumers);
    }

    public static void clear(PartialRenderState block) {
        GpuTexture texture = TEXTURES.remove(block);
        if (texture != null) {
            texture.close();
        }
    }

    @Override
    public void prepare(PartialRenderState partial, GuiRenderState state, int windowScaleFactor) {
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
            RenderSystem.setProjectionMatrix(projectionMatrixBuffer.getBuffer(size, size), ProjectionType.ORTHOGRAPHIC);
            texture.prepare();
            matrices.pushPose();
            if (partial.padding != 0) {
                size -= partial.padding * windowScaleFactor;
            }
            matrices.scale(size, size, size);
            partial.transform(matrices);
            Minecraft mc = Minecraft.getInstance();
            ChunkSectionLayer blockRenderLayer = LayerBakedModel.getBlockRenderLayer(partial.model, () -> ChunkSectionLayer.SOLID);
            RenderType layer = blockRenderLayer == ChunkSectionLayer.TRANSLUCENT ? Sheets.translucentItemSheet() : Sheets.cutoutBlockSheet();
            SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
            mc.getBlockRenderer().renderBatched(
                Blocks.AIR.defaultBlockState(),
                BlockPos.ZERO,
                world,
                matrices,
                bufferSource.getBuffer(layer),
                false,
                List.of(partial.model)
            );
            bufferSource.endBatch();
            matrices.popPose();
            texture.clear();
        }
        state.submitBlitToCurrentLayer(new BlitRenderState(
            RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
            TextureSetup.singleTexture(texture.textureView(), RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST)),
            partial.IDENTITY_POSE,
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
    protected void renderToTexture(PartialRenderState partial, PoseStack matrices) {
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

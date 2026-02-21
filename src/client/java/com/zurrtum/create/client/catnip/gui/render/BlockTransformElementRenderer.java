package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.BlitRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneTorchBlock;

import java.util.IdentityHashMap;
import java.util.Map;

public class BlockTransformElementRenderer extends PictureInPictureRenderer<BlockTransformRenderState> {
    private static final Map<BlockTransformRenderKey, GpuTexture> TEXTURES = new IdentityHashMap<>();
    private final PoseStack matrices = new PoseStack();
    private int windowScaleFactor;

    public BlockTransformElementRenderer(MultiBufferSource.BufferSource vertexConsumers) {
        super(vertexConsumers);
    }

    public static void clear(BlockTransformRenderKey key) {
        GpuTexture texture = TEXTURES.remove(key);
        if (texture != null) {
            texture.close();
        }
    }

    @Override
    public void prepare(BlockTransformRenderState block, GuiRenderState state, int windowScaleFactor) {
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
            texture.prepare(projectionMatrixBuffer);
            matrices.pushPose();
            matrices.translate(size / 2, size / 2, 0);
            if (key.padding != 0) {
                size -= key.padding * windowScaleFactor;
            }
            matrices.scale(size, size, size);
            if (key.zRot != 0) {
                matrices.mulPose(Axis.ZP.rotation(key.zRot));
            }
            if (key.xRot != 0) {
                matrices.mulPose(Axis.XP.rotation(key.xRot));
            }
            if (key.yRot != 0) {
                matrices.mulPose(Axis.YP.rotation(key.yRot));
            }
            matrices.scale(1, -1, 1);
            matrices.translate(-0.5F, -0.5F, -0.5F);
            Minecraft mc = Minecraft.getInstance();
            RenderType layer;
            if (key.state.is(Blocks.REDSTONE_TORCH) && key.state.getValue(RedstoneTorchBlock.LIT)) {
                layer = RenderTypes.cutoutMovingBlock();
            } else {
                layer = ItemBlockRenderTypes.getChunkRenderType(key.state) == ChunkSectionLayer.TRANSLUCENT ? Sheets.translucentItemSheet() : Sheets.cutoutBlockSheet();
            }
            SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
            world.blockState(key.state);
            mc.getBlockRenderer().renderBatched(
                key.state,
                BlockPos.ZERO,
                world,
                matrices,
                bufferSource.getBuffer(layer),
                false,
                key.parts
            );
            bufferSource.endBatch();
            matrices.popPose();
            texture.clear();
        }
        state.submitBlitToCurrentLayer(new BlitRenderState(
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
    protected void renderToTexture(BlockTransformRenderState block, PoseStack matrices) {
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

package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class MillstoneRenderer extends PictureInPictureRenderer<MillstoneRenderState> {
    public MillstoneRenderer(MultiBufferSource.BufferSource vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    protected void renderToTexture(MillstoneRenderState state, PoseStack matrices) {
        Minecraft mc = Minecraft.getInstance();
        mc.gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        matrices.translate(-0.5f, -0.21f, -0.5f);
        matrices.scale(1, -1, -1);
        BlockState blockState;
        List<BlockModelPart> parts;
        BlockRenderDispatcher blockRenderManager = mc.getBlockRenderer();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        VertexConsumer buffer = bufferSource.getBuffer(Sheets.cutoutBlockSheet());

        matrices.pushPose();
        blockState = Blocks.AIR.defaultBlockState();
        world.blockState(blockState);
        parts = List.of(AllPartialModels.MILLSTONE_COG.get());
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.mulPose(Axis.XP.rotationDegrees(22.5f));
        matrices.mulPose(Axis.YP.rotationDegrees(getCurrentAngle()));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        blockRenderManager.renderBatched(blockState, BlockPos.ZERO, world, matrices, buffer, false, parts);
        matrices.popPose();

        blockState = AllBlocks.MILLSTONE.defaultBlockState();
        world.blockState(blockState);
        parts = blockRenderManager.getBlockModel(blockState).collectParts(mc.level.getRandom());
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.mulPose(Axis.XP.rotationDegrees(22.5f));
        matrices.mulPose(Axis.YP.rotationDegrees(22.5f));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        blockRenderManager.renderBatched(blockState, BlockPos.ZERO, world, matrices, buffer, false, parts);
    }

    private static float getCurrentAngle() {
        return (AnimationTickHolder.getRenderTime() * 4f) % 360 * 2;
    }

    @Override
    protected String getTextureLabel() {
        return "Millstone";
    }

    @Override
    public Class<MillstoneRenderState> getRenderStateClass() {
        return MillstoneRenderState.class;
    }
}

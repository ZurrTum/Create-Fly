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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

public class PressBasinRenderer extends PictureInPictureRenderer<PressBasinRenderState> {
    public PressBasinRenderer(MultiBufferSource.BufferSource vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    protected void renderToTexture(PressBasinRenderState state, PoseStack matrices) {
        Minecraft mc = Minecraft.getInstance();
        mc.gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        matrices.scale(1, 1, -1);
        matrices.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrices.mulPose(Axis.YP.rotationDegrees(22.5f));
        matrices.translate(-0.5f, -1.8f, -0.5f);
        matrices.scale(1, -1, 1);

        BlockState blockState;
        List<BlockModelPart> parts;
        BlockRenderDispatcher blockRenderManager = mc.getBlockRenderer();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        VertexConsumer buffer = bufferSource.getBuffer(Sheets.cutoutBlockSheet());
        float time = AnimationTickHolder.getRenderTime();

        blockState = AllBlocks.MECHANICAL_PRESS.defaultBlockState();
        world.blockState(blockState);
        parts = blockRenderManager.getBlockModel(blockState).collectParts(mc.level.getRandom());
        blockRenderManager.renderBatched(blockState, BlockPos.ZERO, world, matrices, buffer, false, parts);

        matrices.pushPose();
        blockState = AllBlocks.SHAFT.defaultBlockState().setValue(BlockStateProperties.AXIS, net.minecraft.core.Direction.Axis.Z);
        world.blockState(blockState);
        parts = blockRenderManager.getBlockModel(blockState).collectParts(mc.level.getRandom());
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.mulPose(Axis.ZP.rotationDegrees(getShaftAngle(time)));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        blockRenderManager.renderBatched(blockState, BlockPos.ZERO, world, matrices, buffer, false, parts);
        matrices.popPose();

        matrices.pushPose();
        blockState = Blocks.AIR.defaultBlockState();
        world.blockState(blockState);
        parts = List.of(AllPartialModels.MECHANICAL_PRESS_HEAD.get());
        matrices.translate(0, getAnimatedHeadOffset(time), 0);
        blockRenderManager.renderBatched(blockState, BlockPos.ZERO, world, matrices, buffer, false, parts);
        matrices.popPose();

        matrices.translate(0, -1.65f, 0);
        blockState = AllBlocks.BASIN.defaultBlockState();
        world.blockState(blockState);
        parts = blockRenderManager.getBlockModel(blockState).collectParts(mc.level.getRandom());
        blockRenderManager.renderBatched(blockState, BlockPos.ZERO, world, matrices, buffer, false, parts);
    }

    private static float getShaftAngle(float time) {
        return (time * 4f) % 360;
    }

    private static float getAnimatedHeadOffset(float time) {
        float cycle = time % 30;
        if (cycle < 10) {
            float progress = cycle / 10;
            return -(progress * progress * progress);
        }
        if (cycle < 15)
            return -1;
        if (cycle < 20)
            return -1 + (1 - ((20 - cycle) / 5));
        return 0;
    }

    @Override
    protected String getTextureLabel() {
        return "Press Basin";
    }

    @Override
    public Class<PressBasinRenderState> getRenderStateClass() {
        return PressBasinRenderState.class;
    }
}
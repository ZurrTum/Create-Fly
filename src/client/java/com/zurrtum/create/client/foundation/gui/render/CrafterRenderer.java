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

public class CrafterRenderer extends PictureInPictureRenderer<CrafterRenderState> {
    public CrafterRenderer(MultiBufferSource.BufferSource vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    protected void renderToTexture(CrafterRenderState state, PoseStack matrices) {
        Minecraft mc = Minecraft.getInstance();
        mc.gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        matrices.scale(1, 1, -1);
        matrices.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrices.mulPose(Axis.YP.rotationDegrees(-22.5f));
        matrices.translate(-0.5f, -0.16f, -0.5f);
        matrices.scale(1, -1, 1);

        BlockState blockState;
        List<BlockModelPart> parts;
        BlockRenderDispatcher blockRenderManager = mc.getBlockRenderer();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        VertexConsumer buffer = bufferSource.getBuffer(Sheets.cutoutBlockSheet());

        blockState = Blocks.AIR.defaultBlockState();
        world.blockState(blockState);
        matrices.pushPose();
        parts = List.of(AllPartialModels.SHAFTLESS_COGWHEEL.get());
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.mulPose(Axis.ZP.rotationDegrees(getCurrentAngle()));
        matrices.mulPose(Axis.XP.rotationDegrees(90));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        blockRenderManager.renderBatched(blockState, BlockPos.ZERO, world, matrices, buffer, false, parts);
        matrices.popPose();

        blockState = AllBlocks.MECHANICAL_CRAFTER.defaultBlockState();
        world.blockState(blockState);
        matrices.pushPose();
        parts = blockRenderManager.getBlockModel(blockState).collectParts(mc.level.getRandom());
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.mulPose(Axis.YP.rotationDegrees(180));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        blockRenderManager.renderBatched(blockState, BlockPos.ZERO, world, matrices, buffer, false, parts);
        matrices.popPose();
    }

    public static float getCurrentAngle() {
        return (AnimationTickHolder.getRenderTime() * 4f) % 360;
    }

    @Override
    protected String getTextureLabel() {
        return "Crafter";
    }

    @Override
    public Class<CrafterRenderState> getRenderStateClass() {
        return CrafterRenderState.class;
    }
}

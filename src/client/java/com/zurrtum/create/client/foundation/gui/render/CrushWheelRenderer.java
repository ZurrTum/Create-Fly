package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

public class CrushWheelRenderer extends PictureInPictureRenderer<CrushWheelRenderState> {
    private final BlockState blockState = AllBlocks.CRUSHING_WHEEL.defaultBlockState().setValue(BlockStateProperties.AXIS, Axis.X);

    public CrushWheelRenderer(MultiBufferSource.BufferSource vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    protected void renderToTexture(CrushWheelRenderState state, PoseStack matrices) {
        Minecraft mc = Minecraft.getInstance();
        mc.gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        matrices.scale(1, 1, -1);
        matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-22.5f));
        matrices.translate(-1.5f, -0.6f, -0.5f);
        matrices.scale(1, -1, 1);

        BlockRenderDispatcher blockRenderManager = mc.getBlockRenderer();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        VertexConsumer buffer = bufferSource.getBuffer(Sheets.cutoutBlockSheet());
        List<BlockModelPart> parts = blockRenderManager.getBlockModel(blockState).collectParts(mc.level.random);
        world.blockState(blockState);

        float angle = getCurrentAngle();
        matrices.pushPose();
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-angle));
        matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        blockRenderManager.renderBatched(blockState, BlockPos.ZERO, world, matrices, buffer, false, parts);
        matrices.popPose();

        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.translate(2, 0, 0);
        matrices.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(angle));
        matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        blockRenderManager.renderBatched(blockState, BlockPos.ZERO, world, matrices, buffer, false, parts);
    }

    public static float getCurrentAngle() {
        return (AnimationTickHolder.getRenderTime() * 4f) % 360;
    }

    @Override
    public Class<CrushWheelRenderState> getRenderStateClass() {
        return CrushWheelRenderState.class;
    }

    @Override
    protected String getTextureLabel() {
        return "Crush Wheel";
    }
}

package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.render.BlockBakedQuadOutput;
import com.zurrtum.create.client.flywheel.lib.model.baked.ModelConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.ModelRenderHelper;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class CrushWheelRenderer extends PictureInPictureRenderer<CrushWheelRenderState> {
    private final BlockState blockState = AllBlocks.CRUSHING_WHEEL.defaultBlockState()
        .setValue(BlockStateProperties.AXIS, Axis.X);
    private final BlockBakedQuadOutput output;

    public CrushWheelRenderer(BufferSource vertexConsumers) {
        super(vertexConsumers);
        output = new BlockBakedQuadOutput(vertexConsumers);
    }

    @Override
    protected void renderToTexture(CrushWheelRenderState state, PoseStack matrices) {
        Minecraft mc = Minecraft.getInstance();
        mc.gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        matrices.scale(1, 1, -1);
        matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-22.5f));
        matrices.translate(-1.5f, -0.6f, -0.5f);
        matrices.scale(1, -1, 1);

        output.setPoseStack(matrices);
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        BlockStateModel model = AllPartialModels.CRUSHING_WHEEL.get();
        world.blockState(blockState);

        float angle = getCurrentAngle();
        matrices.pushPose();
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90));
        matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angle));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        output.updateBuffer(model);
        ModelConsumer blockRenderer = ModelRenderHelper.getHelper(output);
        blockRenderer.tesselateBlock(0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);
        matrices.popPose();

        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.translate(2, 0, 0);
        matrices.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90));
        matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-angle));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        blockRenderer.updateOutput(output);
        blockRenderer.tesselateBlock(0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);
        output.clear();
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

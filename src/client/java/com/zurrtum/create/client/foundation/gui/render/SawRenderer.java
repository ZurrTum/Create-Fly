package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.platform.Lighting.Entry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.render.BlockBakedQuadOutput;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.flywheel.lib.model.baked.ModelConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.ModelRenderHelper;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import com.zurrtum.create.content.kinetics.saw.SawBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class SawRenderer extends PictureInPictureRenderer<SawRenderState> {
    private final BlockBakedQuadOutput output;

    public SawRenderer(BufferSource vertexConsumers) {
        super(vertexConsumers);
        output = new BlockBakedQuadOutput(vertexConsumers);
    }

    @Override
    protected void renderToTexture(SawRenderState state, PoseStack matrices) {
        Minecraft mc = Minecraft.getInstance();
        mc.gameRenderer.getLighting().setupFor(Entry.ENTITY_IN_UI);
        matrices.scale(1, 1, -1);
        matrices.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrices.mulPose(Axis.YP.rotationDegrees(112.5f));
        matrices.translate(-0.5f, -0.2f, -0.5f);
        matrices.scale(1, -1, 1);

        BlockState blockState;
        BlockStateModel model;
        output.setPoseStack(matrices);
        BlockStateModelSet blockStateModelSet = mc.getModelManager().getBlockStateModelSet();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();

        matrices.pushPose();
        blockState = AllBlocks.SHAFT.defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.X);
        world.blockState(blockState);
        model = blockStateModelSet.get(blockState);
        matrices.rotateAround(Axis.XP.rotationDegrees(getCurrentAngle()), 0.5f, 0.5f, 0.5f);
        output.updateBuffer(model);
        ModelConsumer blockRenderer = ModelRenderHelper.getHelper(output);
        blockRenderer.tesselateBlock(0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);
        matrices.popPose();

        blockState = AllBlocks.MECHANICAL_SAW.defaultBlockState().setValue(SawBlock.FACING, Direction.UP);
        world.blockState(blockState);
        model = blockStateModelSet.get(blockState);
        output.updateBuffer(model);
        blockRenderer.updateOutput(output);
        blockRenderer.tesselateBlock(0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);
        output.clear();

        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.mulPose(Axis.ZP.rotationDegrees(-90));
        matrices.mulPose(Axis.YP.rotationDegrees(-90));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        //noinspection deprecation
        CachedBuffers.partial(AllPartialModels.SAW_BLADE_VERTICAL_ACTIVE, Blocks.AIR.defaultBlockState())
            .light(LightCoordsUtil.FULL_BRIGHT)
            .renderInto(matrices.last(), bufferSource.getBuffer(RenderTypes.cutoutMovingBlock()));
    }

    public static float getCurrentAngle() {
        return -(AnimationTickHolder.getRenderTime() * 4.0f) % 360;
    }

    @Override
    protected String getTextureLabel() {
        return "Saw";
    }

    @Override
    public Class<SawRenderState> getRenderStateClass() {
        return SawRenderState.class;
    }

}

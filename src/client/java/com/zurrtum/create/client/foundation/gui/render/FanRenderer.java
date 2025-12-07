package com.zurrtum.create.client.foundation.gui.render;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.compat.sodium.SodiumCompat;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.ComponentChanges;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

import java.util.List;

public class FanRenderer extends SpecialGuiElementRenderer<FanRenderState> {
    private static final Random RANDOM = Random.create();

    public FanRenderer(VertexConsumerProvider.Immediate vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    protected void render(FanRenderState state, MatrixStack matrices) {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ENTITY_IN_UI);
        matrices.scale(1, 1, -1);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-15.5f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(22.5f));
        matrices.translate(-0.92f, -0.75f, -0.5f);
        matrices.scale(1, -1, 1);

        BlockState blockState;
        List<BlockModelPart> parts;
        BlockRenderManager blockRenderManager = mc.getBlockRenderManager();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        VertexConsumer buffer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout());

        matrices.push();
        blockState = Blocks.AIR.getDefaultState();
        parts = List.of(AllPartialModels.ENCASED_FAN_INNER.get());
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(getCurrentAngle() * 16));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, parts);
        matrices.pop();

        matrices.push();
        blockState = AllBlocks.ENCASED_FAN.getDefaultState();
        world.blockState(blockState);
        parts = blockRenderManager.getModel(blockState).getParts(mc.world.random);
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, parts);
        matrices.pop();

        matrices.translate(0, 0, 2);
        blockState = state.target();
        FluidState fluidState = blockState.getFluidState();
        if (!fluidState.isEmpty()) {
            Fluid fluid = fluidState.getFluid();
            SodiumCompat.markFluidSpriteActive(fluid);
            FluidRenderHelper.renderFluidBox(
                fluid,
                ComponentChanges.EMPTY,
                0,
                0,
                0,
                1,
                1,
                1,
                vertexConsumers,
                matrices,
                LightmapTextureManager.MAX_LIGHT_COORDINATE,
                false,
                true
            );
            return;
        }
        world.blockState(blockState);
        RANDOM.setSeed(blockState.getRenderingSeed(BlockPos.ORIGIN));
        parts = blockRenderManager.getModel(blockState).getParts(RANDOM);
        if (blockState.getBlock() instanceof AbstractFireBlock) {
            buffer = vertexConsumers.getBuffer(RenderLayer.getCutout());
        }
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, parts);
    }

    public static float getCurrentAngle() {
        return (AnimationTickHolder.getRenderTime() * 4f) % 360;
    }

    @Override
    protected String getName() {
        return "Fan";
    }

    @Override
    public Class<FanRenderState> getElementClass() {
        return FanRenderState.class;
    }
}

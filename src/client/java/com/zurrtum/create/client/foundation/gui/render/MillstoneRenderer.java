package com.zurrtum.create.client.foundation.gui.render;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;

import java.util.List;

public class MillstoneRenderer extends SpecialGuiElementRenderer<MillstoneRenderState> {
    public MillstoneRenderer(VertexConsumerProvider.Immediate vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    protected void render(MillstoneRenderState state, MatrixStack matrices) {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ENTITY_IN_UI);
        matrices.translate(-0.5f, -0.21f, -0.5f);
        matrices.scale(1, -1, -1);
        BlockState blockState;
        List<BlockModelPart> parts;
        BlockRenderManager blockRenderManager = mc.getBlockRenderManager();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        VertexConsumer buffer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout());

        matrices.push();
        blockState = Blocks.AIR.getDefaultState();
        world.blockState(blockState);
        parts = List.of(AllPartialModels.MILLSTONE_COG.get());
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(22.5f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(getCurrentAngle()));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, parts);
        matrices.pop();

        blockState = AllBlocks.MILLSTONE.getDefaultState();
        world.blockState(blockState);
        parts = blockRenderManager.getModel(blockState).getParts(mc.world.random);
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(22.5f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(22.5f));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, parts);
    }

    private static float getCurrentAngle() {
        return (AnimationTickHolder.getRenderTime() * 4f) % 360 * 2;
    }

    @Override
    protected String getName() {
        return "Millstone";
    }

    @Override
    public Class<MillstoneRenderState> getElementClass() {
        return MillstoneRenderState.class;
    }
}

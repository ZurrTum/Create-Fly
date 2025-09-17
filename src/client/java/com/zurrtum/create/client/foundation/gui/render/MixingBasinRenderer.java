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
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import java.util.List;

public class MixingBasinRenderer extends SpecialGuiElementRenderer<MixingBasinRenderState> {
    public MixingBasinRenderer(VertexConsumerProvider.Immediate vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    protected void render(MixingBasinRenderState state, MatrixStack matrices) {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ENTITY_IN_UI);
        matrices.scale(1, 1, -1);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-15.5f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(22.5f));
        matrices.translate(-0.5f, -1.8f, -0.5f);
        matrices.scale(1, -1, 1);

        BlockState blockState;
        List<BlockModelPart> parts;
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        VertexConsumer buffer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout());
        float time = AnimationTickHolder.getRenderTime();
        float angle = getCurrentAngle(time);

        blockState = AllBlocks.MECHANICAL_MIXER.getDefaultState();
        world.blockState(blockState);
        parts = mc.getBlockRenderManager().getModel(blockState).getParts(mc.world.random);
        mc.getBlockRenderManager().renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, parts);

        blockState = Blocks.AIR.getDefaultState();
        world.blockState(blockState);
        matrices.push();
        parts = List.of(AllPartialModels.SHAFTLESS_COGWHEEL.get());
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle * 2));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        mc.getBlockRenderManager().renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, parts);
        matrices.pop();

        matrices.push();
        matrices.translate(0, getAnimatedHeadOffset(time), 0);
        parts = List.of(AllPartialModels.MECHANICAL_MIXER_POLE.get());
        mc.getBlockRenderManager().renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, parts);
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle * 4));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        parts = List.of(AllPartialModels.MECHANICAL_MIXER_HEAD.get());
        mc.getBlockRenderManager().renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, parts);
        matrices.pop();

        matrices.translate(0, -1.65f, 0);
        blockState = AllBlocks.BASIN.getDefaultState();
        world.blockState(blockState);
        parts = mc.getBlockRenderManager().getModel(blockState).getParts(mc.world.random);
        mc.getBlockRenderManager().renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, parts);
    }

    private static float getCurrentAngle(float time) {
        return (time * 4f) % 360;
    }

    private static float getAnimatedHeadOffset(float time) {
        return -(((MathHelper.sin(time / 32f) + 1) / 5) + .5f);
    }

    @Override
    protected String getName() {
        return "Mixing Basin";
    }

    @Override
    public Class<MixingBasinRenderState> getElementClass() {
        return MixingBasinRenderState.class;
    }
}
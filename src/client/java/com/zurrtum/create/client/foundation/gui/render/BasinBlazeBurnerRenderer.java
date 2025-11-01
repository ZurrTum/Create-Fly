package com.zurrtum.create.client.foundation.gui.render;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import java.util.List;

public class BasinBlazeBurnerRenderer extends SpecialGuiElementRenderer<BasinBlazeBurnerRenderState> {
    public BasinBlazeBurnerRenderer(VertexConsumerProvider.Immediate vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    protected void render(BasinBlazeBurnerRenderState state, MatrixStack matrices) {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ENTITY_IN_UI);
        matrices.scale(1, 1, -1);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-15.5f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(22.5f));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        matrices.scale(1, -1, 1);

        BlockState blockState;
        List<BlockModelPart> parts;
        BlockRenderManager blockRenderManager = mc.getBlockRenderManager();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        VertexConsumer buffer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout());
        float offset = -(MathHelper.sin(AnimationTickHolder.getRenderTime() / 16f) + 0.5f) / 16f;

        blockState = AllBlocks.BLAZE_BURNER.getDefaultState();
        world.blockState(blockState);
        parts = blockRenderManager.getModel(blockState).getParts(mc.world.random);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, parts);

        matrices.push();
        blockState = Blocks.AIR.getDefaultState();
        world.blockState(blockState);
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        boolean seething = state.heat() == HeatLevel.SEETHING;
        parts = List.of((seething ? AllPartialModels.BLAZE_SUPER : AllPartialModels.BLAZE_ACTIVE).get());
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, parts);
        matrices.translate(0, offset, 0);
        parts = List.of((seething ? AllPartialModels.BLAZE_BURNER_SUPER_RODS_2 : AllPartialModels.BLAZE_BURNER_RODS_2).get());
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, parts);
        matrices.pop();


        SpriteShiftEntry spriteShift = seething ? AllSpriteShifts.SUPER_BURNER_FLAME : AllSpriteShifts.BURNER_FLAME;

        float spriteWidth = spriteShift.getTarget().getMaxU() - spriteShift.getTarget().getMinU();

        float spriteHeight = spriteShift.getTarget().getMaxV() - spriteShift.getTarget().getMinV();

        float time = AnimationTickHolder.getRenderTime(mc.world);
        float speed = 1 / 32f + 1 / 64f * state.heat().ordinal();

        double vScroll = speed * time;
        vScroll = vScroll - Math.floor(vScroll);
        vScroll = vScroll * spriteHeight / 2;

        double uScroll = speed * time / 2;
        uScroll = uScroll - Math.floor(uScroll);
        uScroll = uScroll * spriteWidth / 2;

        CachedBuffers.partial(AllPartialModels.BLAZE_BURNER_FLAME, Blocks.AIR.getDefaultState())
            .shiftUVScrolling(spriteShift, (float) uScroll, (float) vScroll).light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
            .renderInto(matrices.peek(), vertexConsumers.getBuffer(RenderLayer.getCutoutMipped()));
    }

    @Override
    protected String getName() {
        return "Blaze Burner";
    }

    @Override
    public Class<BasinBlazeBurnerRenderState> getElementClass() {
        return BasinBlazeBurnerRenderState.class;
    }
}

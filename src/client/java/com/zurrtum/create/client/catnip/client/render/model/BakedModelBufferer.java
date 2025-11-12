package com.zurrtum.create.client.catnip.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.impl.client.render.model.BakedModelBuffererImpl;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class BakedModelBufferer {
    private BakedModelBufferer() {
    }

    public static void bufferModel(
        BlockStateModel model,
        BlockPos pos,
        BlockAndTintGetter level,
        BlockState state,
        @Nullable PoseStack poseStack,
        ShadeSeparatedBufferSource bufferSource
    ) {
        BakedModelBuffererImpl.bufferModel(model, pos, level, state, poseStack, bufferSource);
    }

    public static void bufferModel(
        List<BlockModelPart> parts,
        BlockPos pos,
        BlockAndTintGetter level,
        BlockState state,
        @Nullable PoseStack poseStack,
        ShadeSeparatedResultConsumer resultConsumer
    ) {
        BakedModelBuffererImpl.bufferModel(parts, pos, level, state, poseStack, resultConsumer);
    }

    public static void bufferModel(
        BlockStateModel model,
        BlockPos pos,
        BlockAndTintGetter level,
        BlockState state,
        @Nullable PoseStack poseStack,
        ShadeSeparatedResultConsumer resultConsumer
    ) {
        BakedModelBuffererImpl.bufferModel(model, pos, level, state, poseStack, resultConsumer);
    }

    public static void bufferBlocks(
        Iterator<BlockPos> posIterator,
        BlockAndTintGetter level,
        @Nullable PoseStack poseStack,
        boolean renderFluids,
        ShadeSeparatedBufferSource bufferSource
    ) {
        BakedModelBuffererImpl.bufferBlocks(posIterator, level, poseStack, renderFluids, bufferSource);
    }

    public static void bufferBlocks(
        Iterator<BlockPos> posIterator,
        BlockAndTintGetter level,
        @Nullable PoseStack poseStack,
        boolean renderFluids,
        ShadeSeparatedResultConsumer resultConsumer
    ) {
        BakedModelBuffererImpl.bufferBlocks(posIterator, level, poseStack, renderFluids, resultConsumer);
    }
}

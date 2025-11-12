package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.ExperimentalRedstoneWireEvaluator;
import net.minecraft.world.level.redstone.Orientation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ExperimentalRedstoneWireEvaluator.class)
public class ExperimentalRedstoneControllerMixin {
    @WrapOperation(method = "method_61833(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Ljava/lang/Integer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;neighborChanged(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/redstone/Orientation;Z)V"))
    private void updateNeighbor(
        Level world,
        BlockState state,
        BlockPos neighborPos,
        Block sourceBlock,
        Orientation orientation,
        boolean notify,
        Operation<Void> original,
        @Local(argsOnly = true) BlockPos pos
    ) {
        if (state.getBlock() instanceof NeighborUpdateListeningBlock block) {
            block.neighborUpdate(state, world, neighborPos, sourceBlock, pos, notify);
        }
        original.call(world, state, neighborPos, sourceBlock, orientation, notify);
    }

    @WrapOperation(method = "method_61833(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Ljava/lang/Integer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;neighborChanged(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/redstone/Orientation;)V"))
    private void updateNeighbor(
        Level world,
        BlockPos neighborPos,
        Block sourceBlock,
        Orientation orientation,
        Operation<Void> original,
        @Local(ordinal = 1) BlockPos pos
    ) {
        BlockState state = world.getBlockState(neighborPos);
        if (state.getBlock() instanceof NeighborUpdateListeningBlock block) {
            block.neighborUpdate(state, world, neighborPos, sourceBlock, pos, false);
        }
        original.call(world, neighborPos, sourceBlock, orientation);
    }
}

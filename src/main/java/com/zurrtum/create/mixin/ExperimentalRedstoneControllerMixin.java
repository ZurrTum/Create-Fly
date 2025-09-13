package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ExperimentalRedstoneController;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ExperimentalRedstoneController.class)
public class ExperimentalRedstoneControllerMixin {
    @WrapOperation(method = "method_61833(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/lang/Integer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;updateNeighbor(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/world/block/WireOrientation;Z)V"))
    private void updateNeighbor(
        World world,
        BlockState state,
        BlockPos neighborPos,
        Block sourceBlock,
        WireOrientation orientation,
        boolean notify,
        Operation<Void> original,
        @Local(argsOnly = true) BlockPos pos
    ) {
        if (state.getBlock() instanceof NeighborUpdateListeningBlock block) {
            block.neighborUpdate(state, world, neighborPos, sourceBlock, pos, notify);
        }
        original.call(world, state, neighborPos, sourceBlock, orientation, notify);
    }

    @WrapOperation(method = "method_61833(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/lang/Integer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;updateNeighbor(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/world/block/WireOrientation;)V"))
    private void updateNeighbor(
        World world,
        BlockPos neighborPos,
        Block sourceBlock,
        WireOrientation orientation,
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

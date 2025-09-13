package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DetectorRailBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DetectorRailBlock.class)
public class DetectorRailBlockMixin {
    @WrapOperation(method = "updateNearbyRails(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;updateNeighbor(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/world/block/WireOrientation;Z)V"))
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
}

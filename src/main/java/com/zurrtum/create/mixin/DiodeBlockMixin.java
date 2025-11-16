package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DiodeBlock.class)
public class DiodeBlockMixin {
    @Inject(method = "updateNeighborsInFront(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;neighborChanged(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/redstone/Orientation;)V"))
    private void updateNeighbor(Level world, BlockPos pos, BlockState sourceState, CallbackInfo ci, @Local(ordinal = 1) BlockPos neighborPos) {
        BlockState state = world.getBlockState(neighborPos);
        if (state.getBlock() instanceof NeighborUpdateListeningBlock block) {
            block.neighborUpdate(state, world, neighborPos, (DiodeBlock) (Object) this, pos, false);
        }
    }
}

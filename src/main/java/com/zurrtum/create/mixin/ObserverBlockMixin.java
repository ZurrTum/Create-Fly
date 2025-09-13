package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ObserverBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ObserverBlock.class)
public class ObserverBlockMixin {
    @Inject(method = "updateNeighbors(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;updateNeighbor(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/world/block/WireOrientation;)V"))
    private void updateNeighbor(World world, BlockPos pos, BlockState sourceState, CallbackInfo ci, @Local(ordinal = 1) BlockPos neighborPos) {
        BlockState state = world.getBlockState(neighborPos);
        if (state.getBlock() instanceof NeighborUpdateListeningBlock block) {
            block.neighborUpdate(state, world, neighborPos, (ObserverBlock) (Object) this, pos, false);
        }
    }
}

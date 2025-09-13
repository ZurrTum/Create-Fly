package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.foundation.block.NeighborChangeListeningBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(World.class)
public class WorldMixin {
    @WrapOperation(method = "updateComparators(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", ordinal = 0))
    private BlockState onNeighborChange(World world, BlockPos pos, Operation<BlockState> original, @Local(argsOnly = true) BlockPos neighbor) {
        BlockState state = original.call(world, pos);
        if (state.getBlock() instanceof NeighborChangeListeningBlock block) {
            block.onNeighborChange(state, world, pos, neighbor);
        }
        return state;
    }
}

package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.foundation.block.WeakPowerControlBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.RedstoneView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RedstoneView.class)
public interface RedstoneViewMixin {
    @WrapOperation(method = "getEmittedRedstonePower(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isSolidBlock(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean skip(
        BlockState state,
        BlockView blockView,
        BlockPos pos,
        Operation<Boolean> original,
        @Local(argsOnly = true) Direction direction
    ) {
        if (state.getBlock() instanceof WeakPowerControlBlock block) {
            return block.shouldCheckWeakPower(state, (RedstoneView) this, pos, direction);
        }
        return original.call(state, blockView, pos);
    }
}

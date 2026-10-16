package com.zurrtum.create.mixin;

import com.zurrtum.create.foundation.block.RedStoneConnectBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RedstoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedstoneWireBlock.class)
public class RedStoneWireBlockMixin {
    @Inject(method = "shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z", at = @At("HEAD"), cancellable = true)
    private static void connectsTo(BlockState blockState, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (blockState.getBlock() instanceof RedStoneConnectBlock block) {
            cir.setReturnValue(block.canConnectRedstone(blockState, direction));
        }
    }
}

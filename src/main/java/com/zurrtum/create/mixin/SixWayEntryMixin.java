package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.block.ChainRestrictedNeighborUpdater$SixWayEntry")
public class SixWayEntryMixin {
    @Shadow
    @Final
    private BlockPos pos;

    @Shadow
    @Final
    private Block sourceBlock;

    @Inject(method = "update(Lnet/minecraft/world/World;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/block/NeighborUpdater;tryNeighborUpdate(Lnet/minecraft/world/World;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/world/block/WireOrientation;Z)V"))
    private void neighborUpdate(World world, CallbackInfoReturnable<Boolean> cir, @Local BlockState state, @Local BlockPos pos) {
        if (state.getBlock() instanceof NeighborUpdateListeningBlock block) {
            block.neighborUpdate(state, world, pos, this.sourceBlock, this.pos, false);
        }
    }
}

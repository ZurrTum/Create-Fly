package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.foundation.block.MinecartPassBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartBehavior;
import net.minecraft.world.entity.vehicle.OldMinecartBehavior;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OldMinecartBehavior.class)
public abstract class DefaultMinecartControllerMixin extends MinecartBehavior {
    protected DefaultMinecartControllerMixin(AbstractMinecart minecart) {
        super(minecart);
    }

    @Inject(method = "moveAlongTrack(Lnet/minecraft/server/level/ServerLevel;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/OldMinecartBehavior;setDeltaMovement(DDD)V", ordinal = 0, shift = At.Shift.BY, by = 2))
    private void onMinecartPass(ServerLevel world, CallbackInfo ci, @Local BlockPos pos, @Local BlockState state) {
        if (state.getBlock() instanceof MinecartPassBlock block) {
            block.onMinecartPass(state, world, pos, this.minecart);
        }
    }
}

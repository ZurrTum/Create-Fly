package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.foundation.block.MinecartPassBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.DefaultMinecartController;
import net.minecraft.entity.vehicle.MinecartController;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DefaultMinecartController.class)
public abstract class DefaultMinecartControllerMixin extends MinecartController {
    protected DefaultMinecartControllerMixin(AbstractMinecartEntity minecart) {
        super(minecart);
    }

    @Inject(method = "moveOnRail(Lnet/minecraft/server/world/ServerWorld;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/DefaultMinecartController;setVelocity(DDD)V", ordinal = 0, shift = At.Shift.BY, by = 2))
    private void onMinecartPass(ServerWorld world, CallbackInfo ci, @Local BlockPos pos, @Local BlockState state) {
        if (state.getBlock() instanceof MinecartPassBlock block) {
            block.onMinecartPass(state, world, pos, this.minecart);
        }
    }
}

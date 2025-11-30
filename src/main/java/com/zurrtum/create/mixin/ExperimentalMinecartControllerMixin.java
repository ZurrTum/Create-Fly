package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.foundation.block.MinecartPassBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.ExperimentalMinecartController;
import net.minecraft.entity.vehicle.ExperimentalMinecartController.MoveIteration;
import net.minecraft.entity.vehicle.MinecartController;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperimentalMinecartController.class)
public abstract class ExperimentalMinecartControllerMixin extends MinecartController {
    protected ExperimentalMinecartControllerMixin(AbstractMinecartEntity minecart) {
        super(minecart);
    }

    @Inject(method = "moveOnRail(Lnet/minecraft/server/world/ServerWorld;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/AbstractMinecartEntity;moveAlongTrack(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/enums/RailShape;D)D"))
    private void onMinecartPass(
        ServerWorld level,
        CallbackInfo ci,
        @Local MoveIteration trackIteration,
        @Local BlockPos pos,
        @Local BlockState state
    ) {
        if (trackIteration.initial && state.getBlock() instanceof MinecartPassBlock block) {
            block.onMinecartPass(state, level, pos, minecart);
        }
    }
}

package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.client.content.equipment.armor.RemainingAirOverlay;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @WrapOperation(method = "baseTick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isAlive()Z", ordinal = 0))
    private boolean clientTick(LivingEntity entity, Operation<Boolean> original) {
        if (original.call(entity)) {
            Level world = entity.level();
            if (world.isClientSide() && entity instanceof LocalPlayer clientPlayer) {
                RemainingAirOverlay.update(clientPlayer, world);
            }
            return true;
        }
        return false;
    }
}

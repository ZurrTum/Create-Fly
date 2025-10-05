package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.client.content.equipment.armor.RemainingAirOverlay;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @WrapOperation(method = "baseTick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isAlive()Z", ordinal = 0))
    private boolean clientTick(LivingEntity entity, Operation<Boolean> original) {
        if (original.call(entity)) {
            World world = entity.getEntityWorld();
            if (world.isClient() && entity instanceof ClientPlayerEntity clientPlayer) {
                RemainingAirOverlay.update(clientPlayer, world);
            }
            return true;
        }
        return false;
    }
}

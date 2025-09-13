package com.zurrtum.create.mixin;

import com.zurrtum.create.content.equipment.bell.HauntedBellPulser;
import com.zurrtum.create.content.equipment.wrench.WrenchItem;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void tickPost(CallbackInfo ci) {
        HauntedBellPulser.hauntedBellCreatesPulse((ServerPlayerEntity) (Object) this);
    }

    @Inject(method = "attack(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void attack(Entity target, CallbackInfo ci) {
        if (WrenchItem.wrenchInstaKillsMinecarts((ServerPlayerEntity) (Object) this, target)) {
            ci.cancel();
        }
    }
}

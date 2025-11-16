package com.zurrtum.create.mixin;

import com.zurrtum.create.content.equipment.bell.HauntedBellPulser;
import com.zurrtum.create.content.equipment.wrench.WrenchItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void tickPost(CallbackInfo ci) {
        HauntedBellPulser.hauntedBellCreatesPulse((ServerPlayer) (Object) this);
    }

    @Inject(method = "attack(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void attack(Entity target, CallbackInfo ci) {
        if (WrenchItem.wrenchInstaKillsMinecarts((ServerPlayer) (Object) this, target)) {
            ci.cancel();
        }
    }
}

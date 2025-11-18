package com.zurrtum.create.mixin;

import com.zurrtum.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.level.ServerLevel$EntityCallbacks")
public class EntityCallbacksMixin {
    @Final
    @Shadow
    ServerLevel this$0;

    @Inject(method = "onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"))
    private void stopTracking(Entity entity, CallbackInfo ci) {
        if (!entity.isAlive()) {
            CapabilityMinecartController.onEntityDeath(this$0, entity);
        }
    }
}

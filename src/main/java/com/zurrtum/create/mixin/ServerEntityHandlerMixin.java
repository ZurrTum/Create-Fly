package com.zurrtum.create.mixin;

import com.zurrtum.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.world.ServerWorld$ServerEntityHandler")
public class ServerEntityHandlerMixin {
    @Final
    @Shadow
    ServerWorld field_26936;

    @Inject(method = "stopTracking(Lnet/minecraft/entity/Entity;)V", at = @At("TAIL"))
    private void stopTracking(Entity entity, CallbackInfo ci) {
        if (!entity.isAlive()) {
            CapabilityMinecartController.onEntityDeath(field_26936, entity);
        }
    }
}

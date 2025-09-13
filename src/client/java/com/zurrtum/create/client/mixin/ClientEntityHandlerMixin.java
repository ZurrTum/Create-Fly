package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.flywheel.impl.visualization.VisualizationEventHandler;
import com.zurrtum.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.world.ClientWorld$ClientEntityHandler")
public class ClientEntityHandlerMixin {
    @Shadow
    @Final
    ClientWorld field_27735;

    @Inject(method = "stopTracking(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"))
    private void onEntityLeaveLevel(Entity entity, CallbackInfo ci) {
        VisualizationEventHandler.onEntityLeaveLevel(field_27735, entity);
        CapabilityMinecartController.onEntityDeath(field_27735, entity);
    }
}

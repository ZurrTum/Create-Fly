package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.content.trains.entity.CarriageEntityHandler;
import net.minecraft.entity.Entity;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.world.ServerEntityManager$Listener")
public class ServerEntityManagerListenerMixin<T extends EntityLike> {
    @Shadow
    @Final
    private T entity;

    @Shadow
    private long sectionPos;

    @Inject(method = "updateEntityPosition()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerEntityManager$Listener;updateLoadStatus(Lnet/minecraft/world/entity/EntityTrackingStatus;Lnet/minecraft/world/entity/EntityTrackingStatus;)V"))
    private void onEnteringSection(CallbackInfo ci, @Local long oldPos) {
        if (entity instanceof Entity realEntity) {
            CarriageEntityHandler.onEntityEnterSection(realEntity, oldPos, sectionPos);
        }
    }
}

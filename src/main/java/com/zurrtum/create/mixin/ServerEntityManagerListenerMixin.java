package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.content.trains.entity.CarriageEntityHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.level.entity.PersistentEntitySectionManager$Callback")
public class ServerEntityManagerListenerMixin<T extends EntityAccess> {
    @Shadow
    @Final
    private T entity;

    @Shadow
    private long currentSectionKey;

    @Inject(method = "onMove()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/entity/PersistentEntitySectionManager$Callback;updateStatus(Lnet/minecraft/world/level/entity/Visibility;Lnet/minecraft/world/level/entity/Visibility;)V"))
    private void onEnteringSection(CallbackInfo ci, @Local long oldPos) {
        if (entity instanceof Entity realEntity) {
            CarriageEntityHandler.onEntityEnterSection(realEntity, oldPos, currentSectionKey);
        }
    }
}

package com.zurrtum.create.mixin;

import com.zurrtum.create.AllDamageSources;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.damagesource.DamageSources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DamageSources.class)
public class DamageSourcesMixin {
    @Inject(method = "<init>(Lnet/minecraft/core/RegistryAccess;)V", at = @At("TAIL"))
    private void register(RegistryAccess registryManager, CallbackInfo ci) {
        AllDamageSources.register(registryManager);
    }
}

package com.zurrtum.create.mixin;

import com.zurrtum.create.AllDamageSources;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.registry.DynamicRegistryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DamageSources.class)
public class DamageSourcesMixin {
    @Inject(method = "<init>(Lnet/minecraft/registry/DynamicRegistryManager;)V", at = @At("TAIL"))
    private void register(DynamicRegistryManager registryManager, CallbackInfo ci) {
        AllDamageSources.register(registryManager);
    }
}

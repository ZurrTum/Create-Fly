package com.zurrtum.create.mixin;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.zurrtum.create.Create.MOD_ID;

@Mixin(RegistryKeys.class)
public class RegistryKeysMixin {
    @Inject(method = "getPath(Lnet/minecraft/registry/RegistryKey;)Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
    private static void getPath(RegistryKey<? extends Registry<?>> registryRef, CallbackInfoReturnable<String> cir) {
        Identifier id = registryRef.getValue();
        if (id.getNamespace().equals(MOD_ID)) {
            cir.setReturnValue(id.getPath());
        }
    }

    @Inject(method = "getTagPath(Lnet/minecraft/registry/RegistryKey;)Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
    private static void getTagPath(RegistryKey<? extends Registry<?>> registryRef, CallbackInfoReturnable<String> cir) {
        Identifier id = registryRef.getValue();
        if (id.getNamespace().equals(MOD_ID)) {
            cir.setReturnValue("tags/" + id.getPath());
        }
    }
}

package com.zurrtum.create.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.zurrtum.create.Create.MOD_ID;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

@Mixin(Registries.class)
public class RegistryKeysMixin {
    @Inject(method = "elementsDirPath(Lnet/minecraft/resources/ResourceKey;)Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
    private static void getPath(ResourceKey<? extends Registry<?>> registryRef, CallbackInfoReturnable<String> cir) {
        ResourceLocation id = registryRef.location();
        if (id.getNamespace().equals(MOD_ID)) {
            cir.setReturnValue(id.getPath());
        }
    }

    @Inject(method = "tagsDirPath(Lnet/minecraft/resources/ResourceKey;)Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
    private static void getTagPath(ResourceKey<? extends Registry<?>> registryRef, CallbackInfoReturnable<String> cir) {
        ResourceLocation id = registryRef.location();
        if (id.getNamespace().equals(MOD_ID)) {
            cir.setReturnValue("tags/" + id.getPath());
        }
    }
}

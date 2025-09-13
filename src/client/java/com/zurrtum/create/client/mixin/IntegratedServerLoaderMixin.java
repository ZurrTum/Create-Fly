package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Dynamic;
import com.zurrtum.create.infrastructure.worldgen.AllPlacedFeatures;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.integrated.IntegratedServerLoader;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(IntegratedServerLoader.class)
public class IntegratedServerLoaderMixin {
    @WrapOperation(method = "start(Lnet/minecraft/world/level/storage/LevelStorage$Session;Lcom/mojang/serialization/Dynamic;ZLjava/lang/Runnable;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/integrated/IntegratedServerLoader;load(Lcom/mojang/serialization/Dynamic;ZLnet/minecraft/resource/ResourcePackManager;)Lnet/minecraft/server/SaveLoader;"))
    private SaveLoader addBiomeFeatures(
        IntegratedServerLoader instance,
        Dynamic<?> levelProperties,
        boolean safeMode,
        ResourcePackManager dataPackManager,
        Operation<SaveLoader> original
    ) {
        SaveLoader loader = original.call(instance, levelProperties, safeMode, dataPackManager);
        AllPlacedFeatures.register(loader.combinedDynamicRegistries().getCombinedRegistryManager());
        return loader;
    }

    @WrapOperation(method = "startNewWorld(Lnet/minecraft/world/level/storage/LevelStorage$Session;Lnet/minecraft/server/DataPackContents;Lnet/minecraft/registry/CombinedDynamicRegistries;Lnet/minecraft/world/SaveProperties;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;startIntegratedServer(Lnet/minecraft/world/level/storage/LevelStorage$Session;Lnet/minecraft/resource/ResourcePackManager;Lnet/minecraft/server/SaveLoader;Z)V"))
    private void addBiomeFeatures(
        MinecraftClient instance,
        LevelStorage.Session session,
        ResourcePackManager dataPackManager,
        SaveLoader saveLoader,
        boolean newWorld,
        Operation<Void> original
    ) {
        AllPlacedFeatures.register(saveLoader.combinedDynamicRegistries().getCombinedRegistryManager());
        original.call(instance, session, dataPackManager, saveLoader, newWorld);
    }
}

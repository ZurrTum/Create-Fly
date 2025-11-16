package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Dynamic;
import com.zurrtum.create.infrastructure.worldgen.AllPlacedFeatures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WorldOpenFlows.class)
public class WorldOpenFlowsMixin {
    @WrapOperation(method = "openWorldLoadLevelStem(Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lcom/mojang/serialization/Dynamic;ZLjava/lang/Runnable;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/worldselection/WorldOpenFlows;loadWorldStem(Lcom/mojang/serialization/Dynamic;ZLnet/minecraft/server/packs/repository/PackRepository;)Lnet/minecraft/server/WorldStem;"))
    private WorldStem addBiomeFeatures(
        WorldOpenFlows instance,
        Dynamic<?> levelProperties,
        boolean safeMode,
        PackRepository dataPackManager,
        Operation<WorldStem> original
    ) {
        WorldStem loader = original.call(instance, levelProperties, safeMode, dataPackManager);
        AllPlacedFeatures.register(loader.registries().compositeAccess());
        return loader;
    }

    @WrapOperation(method = "createLevelFromExistingSettings(Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lnet/minecraft/server/ReloadableServerResources;Lnet/minecraft/core/LayeredRegistryAccess;Lnet/minecraft/world/level/storage/WorldData;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;doWorldLoad(Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lnet/minecraft/server/packs/repository/PackRepository;Lnet/minecraft/server/WorldStem;Z)V"))
    private void addBiomeFeatures(
        Minecraft instance,
        LevelStorageSource.LevelStorageAccess session,
        PackRepository dataPackManager,
        WorldStem saveLoader,
        boolean newWorld,
        Operation<Void> original
    ) {
        AllPlacedFeatures.register(saveLoader.registries().compositeAccess());
        original.call(instance, session, dataPackManager, saveLoader, newWorld);
    }
}

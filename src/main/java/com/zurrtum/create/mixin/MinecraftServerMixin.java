package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.DataFixer;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.WorldAttached;
import com.zurrtum.create.content.contraptions.ContraptionHandler;
import com.zurrtum.create.content.contraptions.actors.trainControls.ControlsServerHandler;
import com.zurrtum.create.content.contraptions.minecart.CouplingPhysics;
import com.zurrtum.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import com.zurrtum.create.content.kinetics.chainConveyor.ServerChainConveyorHandler;
import com.zurrtum.create.content.kinetics.drill.CobbleGenOptimisation;
import com.zurrtum.create.content.redstone.link.controller.LinkedControllerServerHandler;
import com.zurrtum.create.foundation.utility.ServerSpeedProvider;
import com.zurrtum.create.foundation.utility.TickBasedCache;
import com.zurrtum.create.infrastructure.worldgen.AllPlacedFeatures;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow
    public abstract RegistryAccess.Frozen registryAccess();

    @Inject(method = "tickServer(Ljava/util/function/BooleanSupplier;)V", at = @At("TAIL"))
    void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        Create.SCHEMATIC_RECEIVER.tick();
        ServerSpeedProvider.serverTick(server);
        Create.RAILWAYS.sync.serverTick(server);
        ServerChainConveyorHandler.tick(server);
        TickBasedCache.tick();
    }

    @Inject(method = "runServer()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;stopServer()V"))
    private void serverStopping(CallbackInfo ci) {
        Create.SCHEMATIC_RECEIVER.shutdown();
    }

    @Inject(method = "tickChildren(Ljava/util/function/BooleanSupplier;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;tick(Ljava/util/function/BooleanSupplier;)V", shift = At.Shift.AFTER))
    private void onServerWorldTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci, @Local ServerLevel world) {
        ContraptionHandler.tick(world);
        CapabilityMinecartController.tick(world);
        CouplingPhysics.tick(world);
        LinkedControllerServerHandler.tick(world);
        ControlsServerHandler.tick(world);
        Create.RAILWAYS.tick(world);
        Create.LOGISTICS.tick(world);
    }

    @WrapOperation(method = "createLevels()V", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private <K, V> V onLoad(Map<K, V> map, K key, V value, Operation<V> original) {
        V result = original.call(map, key, value);
        Level world = (Level) value;
        Create.REDSTONE_LINK_NETWORK_HANDLER.onLoadWorld(world);
        Create.TORQUE_PROPAGATOR.onLoadWorld(world);
        return result;
    }

    @Inject(method = "createLevels()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/ServerLevelData;isInitialized()Z"))
    private void onLoadOverworld(CallbackInfo ci, @Local ServerLevel world) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        Create.RAILWAYS.levelLoaded(server);
        Create.LOGISTICS.levelLoaded(server);
    }

    @Inject(method = "stopServer()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;close()V"))
    private void onUnload(CallbackInfo ci, @Local ServerLevel world) {
        Create.REDSTONE_LINK_NETWORK_HANDLER.onUnloadWorld(world);
        Create.TORQUE_PROPAGATOR.onUnloadWorld(world);
        WorldAttached.invalidateWorld(world);
        CobbleGenOptimisation.invalidateWorld(world);
    }

    @Inject(method = "runServer()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;onServerExit()V"))
    private void onStopServer(CallbackInfo ci) {
        Create.SERVER = null;
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/LayeredRegistryAccess;compositeAccess()Lnet/minecraft/core/RegistryAccess$Frozen;", ordinal = 0))
    private void addBiomeFeatures(
        Thread serverThread,
        LevelStorageSource.LevelStorageAccess storageSource,
        PackRepository packRepository,
        WorldStem worldStem,
        Optional<GameRules> gameRules,
        Proxy proxy,
        DataFixer fixerUpper,
        Services services,
        LevelLoadListener levelLoadListener,
        CallbackInfo ci
    ) {
        if ((Object) this instanceof DedicatedServer) {
            AllPlacedFeatures.register(registryAccess());
        }
    }
}

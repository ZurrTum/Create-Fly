package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.zurrtum.create.infrastructure.resources.NetworkLoadTask;
import com.zurrtum.create.infrastructure.resources.ResourceLoader;
import net.minecraft.resources.*;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(NetworkRegistryLoadTask.class)
public abstract class NetworkRegistryLoadTaskMixin<T> extends RegistryLoadTask<T> {
    private NetworkRegistryLoadTaskMixin(
        RegistryDataLoader.RegistryData<T> data,
        Lifecycle lifecycle,
        Map<ResourceKey<?>, Exception> loadingErrors
    ) {
        super(data, lifecycle, loadingErrors);
    }

    @Inject(method = "load(Lnet/minecraft/resources/RegistryOps$RegistryInfoLookup;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;", at = @At(value = "FIELD", target = "Lnet/minecraft/nbt/NbtOps;INSTANCE:Lnet/minecraft/nbt/NbtOps;", opcode = Opcodes.GETSTATIC))
    private void init(
        RegistryOps.RegistryInfoLookup context,
        Executor executor,
        CallbackInfoReturnable<CompletableFuture<?>> cir,
        @Share("task") LocalRef<NetworkLoadTask<T>> ref
    ) {
        ResourceKey<?> key = registryKey();
        ref.set(ResourceLoader.createNetworkTask(key, data));
    }

    @ModifyArg(method = "load(Lnet/minecraft/resources/RegistryOps$RegistryInfoLookup;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private Supplier<PendingRegistration<T>> replace(
        Supplier<PendingRegistration<T>> supplier,
        @Local ResourceKey<T> key,
        @Share("task") LocalRef<NetworkLoadTask<T>> ref
    ) {
        NetworkLoadTask<T> task = ref.get();
        if (task != null) {
            return task.replace(key.identifier(), supplier);
        }
        return supplier;
    }

    @SuppressWarnings("unchecked")
    @WrapOperation(method = "lambda$load$1(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/resources/RegistryOps;Lnet/minecraft/resources/FileToIdConverter;)Lnet/minecraft/resources/RegistryLoadTask$PendingRegistration;", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/RegistryDataLoader$RegistryData;elementCodec()Lcom/mojang/serialization/Codec;"))
    private Codec<T> elementCodec(RegistryDataLoader.RegistryData<T> instance, Operation<Codec<T>> original) {
        Codec<?> codec = ResourceLoader.SCOPED_VALUE.orElse(ResourceLoader.EMPTY);
        if (codec != ResourceLoader.EMPTY) {
            return (Codec<T>) codec;
        }
        return original.call(instance);
    }
}

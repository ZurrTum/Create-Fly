package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.zurrtum.create.content.processing.RecipeLoader;
import com.zurrtum.create.infrastructure.resources.ResourceLoader;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

@Mixin(ResourceManagerRegistryLoadTask.class)
public abstract class ResourceManagerRegistryLoadTaskMixin<T> extends RegistryLoadTask<T> {
    private ResourceManagerRegistryLoadTaskMixin(
        RegistryDataLoader.RegistryData<T> data,
        Lifecycle lifecycle,
        Map<ResourceKey<?>, Exception> loadingErrors
    ) {
        super(data, lifecycle, loadingErrors);
    }

    @ModifyArg(method = "lambda$load$1(Lnet/minecraft/resources/RegistryOps$RegistryInfoLookup;Lnet/minecraft/resources/FileToIdConverter;Ljava/util/concurrent/Executor;Ljava/util/Map;)Ljava/util/concurrent/CompletionStage;", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/thread/ParallelMapTransform;schedule(Ljava/util/Map;Ljava/util/function/BiFunction;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"), index = 1)
    private <V> BiFunction<Identifier, Resource, V> replace(
        BiFunction<Identifier, Resource, V> operation
    ) {
        ResourceKey<?> key = registryKey();
        if (key == Registries.RECIPE) {
            return RecipeLoader.createTask(operation);
        }
        return ResourceLoader.replace(key, operation, data);
    }

    @SuppressWarnings("unchecked")
    @WrapOperation(method = "lambda$load$2(Lnet/minecraft/resources/FileToIdConverter;Lnet/minecraft/resources/RegistryOps;Lnet/minecraft/resources/Identifier;Lnet/minecraft/server/packs/resources/Resource;)Lnet/minecraft/resources/RegistryLoadTask$PendingRegistration;", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/RegistryDataLoader$RegistryData;elementCodec()Lcom/mojang/serialization/Codec;"))
    private Codec<T> elementCodec(RegistryDataLoader.RegistryData<T> instance, Operation<Codec<T>> original) {
        Codec<?> codec = ResourceLoader.SCOPED_VALUE.orElse(ResourceLoader.EMPTY);
        if (codec != ResourceLoader.EMPTY) {
            return (Codec<T>) codec;
        }
        return original.call(instance);
    }

    @ModifyArg(method = "lambda$load$3(Ljava/util/Map;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceManagerRegistryLoadTask;registerElements(Ljava/util/stream/Stream;)V"))
    private Stream<PendingRegistration<T>> addRecipes(
        Stream<PendingRegistration<T>> stream
    ) {
        ResourceKey<?> key = registryKey();
        if (key == Registries.RECIPE) {
            return RecipeLoader.onLoaded(stream);
        }
        return stream;
    }
}

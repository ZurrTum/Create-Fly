package com.zurrtum.create.mixin;

import com.mojang.serialization.Lifecycle;
import com.zurrtum.create.content.processing.RecipeLoader;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryLoadTask;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceManagerRegistryLoadTask;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Map;
import java.util.Optional;
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

    @SuppressWarnings("unchecked")
    @ModifyArg(method = "lambda$load$1(Lnet/minecraft/resources/RegistryOps$RegistryInfoLookup;Lnet/minecraft/resources/FileToIdConverter;Ljava/util/concurrent/Executor;Ljava/util/Map;)Ljava/util/concurrent/CompletionStage;", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/thread/ParallelMapTransform;schedule(Ljava/util/Map;Ljava/util/function/BiFunction;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"), index = 1)
    private <K, U, V> BiFunction<K, U, V> replace(
        BiFunction<K, U, V> operation
    ) {
        if (registryKey().identifier().getPath().equals("recipe")) {
            RecipeLoader.recipes.clear();
            return (t, u) -> {
                V value = operation.apply(t, u);
                PendingRegistration<Recipe<?>> entry = (PendingRegistration<Recipe<?>>) value;
                Optional<Recipe<?>> data = entry.value().left();
                if (data.isPresent()) {
                    RecipeLoader.onLoadRecipe(entry, data.get());
                }
                return value;
            };
        }
        return operation;
    }

    @ModifyArg(method = "lambda$load$3(Ljava/util/Map;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceManagerRegistryLoadTask;registerElements(Ljava/util/stream/Stream;)V"))
    private Stream<PendingRegistration<T>> addRecipes(
        Stream<PendingRegistration<T>> stream
    ) {
        if (registryKey().identifier().getPath().equals("recipe")) {
            return RecipeLoader.onLoaded(stream);
        }
        return stream;
    }
}

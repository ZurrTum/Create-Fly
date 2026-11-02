package com.zurrtum.create.content.processing;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Lifecycle;
import com.zurrtum.create.content.kinetics.mixer.PotionRecipe;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryLoadTask.PendingRegistration;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.crafting.BrewingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static com.zurrtum.create.Create.MOD_ID;

public class RecipeLoader {
    public static final ConcurrentLinkedQueue<PendingRegistration<Recipe<?>>> recipes = new ConcurrentLinkedQueue<>();
    public static final RegistrationInfo registrationInfo = new RegistrationInfo(
        Optional.of(new KnownPack(
            MOD_ID,
            "recipes",
            FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata().getVersion()
                .getFriendlyString()
        )), Lifecycle.stable()
    );

    public static void addRecipe(Identifier id, Recipe<?> recipe) {
        recipes.add(new PendingRegistration<>(
            ResourceKey.create(Registries.RECIPE, id),
            Either.left(recipe),
            registrationInfo
        ));
    }

    public static void onLoadRecipe(PendingRegistration<Recipe<?>> value, Recipe<?> recipe) {
        RecipeType<? extends Recipe<?>> type = recipe.getType();
        if (type == RecipeType.BREWING) {
            PotionRecipe.createFromBrewing(value, (BrewingRecipe) recipe);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Stream<PendingRegistration<T>> onLoaded(Stream<PendingRegistration<T>> stream) {
        return Stream.concat(stream, (Stream<PendingRegistration<T>>) (Object) recipes.stream());
    }

    @SuppressWarnings("unchecked")
    public static <V> BiFunction<Identifier, Resource, V> createTask(BiFunction<Identifier, Resource, V> operation) {
        RecipeLoader.recipes.clear();
        return (BiFunction<Identifier, Resource, V>) new RecipeLoaderTask((BiFunction<Identifier, Resource, PendingRegistration<Recipe<?>>>) operation);
    }
}

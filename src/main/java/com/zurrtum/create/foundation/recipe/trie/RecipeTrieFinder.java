package com.zurrtum.create.foundation.recipe.trie;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.zurrtum.create.foundation.recipe.RecipeFinder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class RecipeTrieFinder {
    private static final Cache<Object, RecipeTrie<Recipe<?>>> CACHED_TRIES = CacheBuilder.newBuilder().build();

    public static RecipeTrie<Recipe<?>> get(
        @NotNull Object cacheKey,
        ServerLevel world,
        Predicate<RecipeHolder<? extends Recipe<?>>> conditions
    ) throws ExecutionException {
        return CACHED_TRIES.get(
            cacheKey, () -> {
                List<RecipeHolder<? extends Recipe<?>>> list = RecipeFinder.get(cacheKey, world, conditions);

                RecipeTrie.Builder<Recipe<?>> builder = RecipeTrie.builder();
                for (RecipeHolder<? extends Recipe<?>> recipe : list) {
                    builder.insert(recipe.value());
                }

                return builder.build();
            }
        );
    }

    public static final ResourceManagerReloadListener LISTENER = resourceManager -> CACHED_TRIES.invalidateAll();
}
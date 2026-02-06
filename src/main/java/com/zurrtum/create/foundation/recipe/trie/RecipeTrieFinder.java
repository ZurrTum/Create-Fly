package com.zurrtum.create.foundation.recipe.trie;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.zurrtum.create.foundation.recipe.RecipeFinder;
import com.zurrtum.create.foundation.utility.CreateResourceReloader;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

public class RecipeTrieFinder {
    private static final Cache<Object, RecipeTrie<Recipe<?>>> CACHED_TRIES = CacheBuilder.newBuilder().build();

    public static RecipeTrie<Recipe<?>> get(
        @NotNull Object cacheKey,
        ServerWorld world,
        Predicate<RecipeEntry<? extends Recipe<?>>> conditions
    ) throws ExecutionException {
        return CACHED_TRIES.get(
            cacheKey, () -> {
                List<RecipeEntry<? extends Recipe<?>>> list = RecipeFinder.get(cacheKey, world, conditions);

                RecipeTrie.Builder<Recipe<?>> builder = RecipeTrie.builder();
                for (RecipeEntry<? extends Recipe<?>> recipe : list) {
                    builder.insert(recipe.value());
                }

                return builder.build();
            }
        );
    }

    public static final SynchronousResourceReloader LISTENER = new ReloadListener();

    private static class ReloadListener extends CreateResourceReloader {
        public ReloadListener() {
            super("recipe_trie");
        }

        @Override
        public void reload(ResourceManager resourceManager) {
            CACHED_TRIES.invalidateAll();
        }
    }
}
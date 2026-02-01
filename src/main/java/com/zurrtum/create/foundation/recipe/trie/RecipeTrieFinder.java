package com.zurrtum.create.foundation.recipe.trie;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.zurrtum.create.foundation.recipe.RecipeFinder;
import com.zurrtum.create.foundation.utility.CreateResourceReloader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

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

    public static final ResourceManagerReloadListener LISTENER = new ReloadListener();

    private static class ReloadListener extends CreateResourceReloader {
        public ReloadListener() {
            super("recipe_trie");
        }

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            CACHED_TRIES.invalidateAll();
        }
    }
}
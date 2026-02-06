package com.zurrtum.create.foundation.recipe;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.zurrtum.create.Create;
import com.zurrtum.create.foundation.utility.CreateResourceReloader;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

/**
 * Utility for searching through a level's recipe collection.
 * Non-dynamic conditions can be split off into an initial search for caching intermediate results.
 *
 * @author simibubi
 */
public class RecipeFinder {
    private static final Cache<Object, List<RecipeEntry<?>>> CACHED_SEARCHES = CacheBuilder.newBuilder().build();

    public static final SynchronousResourceReloader LISTENER = new ReloadListener();

    /**
     * Find all recipes matching the condition predicate.
     * If this search is made more than once,
     * using the same object instance as the cacheKey will retrieve the cached result from the first search.
     *
     * @param cacheKey (can be null to prevent the caching)
     * @return A started search to continue with more specific conditions.
     */
    public static List<RecipeEntry<?>> get(@Nullable Object cacheKey, ServerWorld level, Predicate<RecipeEntry<?>> conditions) {
        if (cacheKey == null)
            return startSearch(level, conditions);

        try {
            return CACHED_SEARCHES.get(cacheKey, () -> startSearch(level, conditions));
        } catch (ExecutionException e) {
            Create.LOGGER.error("Encountered a exception while searching for recipes", e);
        }

        return Collections.emptyList();
    }

    private static List<RecipeEntry<?>> startSearch(ServerWorld level, Predicate<? super RecipeEntry<?>> conditions) {
        List<RecipeEntry<?>> recipes = new ArrayList<>();
        for (RecipeEntry<?> r : level.getRecipeManager().values())
            if (conditions.test(r))
                recipes.add(r);
        return recipes;
    }

    private static class ReloadListener extends CreateResourceReloader {
        public ReloadListener() {
            super("recipe_search");
        }

        @Override
        public void reload(ResourceManager resourceManager) {
            CACHED_SEARCHES.invalidateAll();
        }
    }
}
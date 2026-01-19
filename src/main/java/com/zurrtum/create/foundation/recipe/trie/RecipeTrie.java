package com.zurrtum.create.foundation.recipe.trie;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.zurrtum.create.Create;
import com.zurrtum.create.content.processing.basin.BasinRecipe;
import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class RecipeTrie<R extends Recipe<?>> {
    private static final int MAX_CACHE_SIZE = Integer.getInteger("create.recipe_trie.max_cache_size", 512);

    private final IntArrayTrie<R> trie;
    private final Object2IntMap<AbstractVariant> variantToId;
    private final Int2ObjectMap<IntSet> variantToIngredients;
    private final int universalIngredientId;

    private final Cache<Set<AbstractVariant>, IntSet> ingredientCache = CacheBuilder.newBuilder().maximumSize(MAX_CACHE_SIZE).build();

    private RecipeTrie(
        IntArrayTrie<R> trie,
        Object2IntMap<AbstractVariant> variantToId,
        Int2ObjectMap<IntSet> variantToIngredients,
        int universalIngredientId
    ) {
        this.trie = trie;
        this.variantToId = variantToId;
        this.variantToIngredients = variantToIngredients;
        this.universalIngredientId = universalIngredientId;
    }

    public static Set<AbstractVariant> getVariants(@Nullable Container itemStorage, @Nullable FluidInventory fluidStorage) {
        Set<AbstractVariant> variants = new HashSet<>();

        if (itemStorage != null) {
            for (int slot = 0, size = itemStorage.getContainerSize(); slot < size; slot++) {
                ItemStack item = itemStorage.getItem(slot);
                if (item.isEmpty())
                    continue;

                variants.add(new AbstractVariant.AbstractItem(item.getItem()));
            }
        }

        if (fluidStorage != null) {
            for (int tank = 0, size = fluidStorage.size(); tank < size; tank++) {
                FluidStack fluid = fluidStorage.getStack(tank);
                if (fluid.isEmpty())
                    continue;

                variants.add(new AbstractVariant.AbstractFluid(fluid.getFluid()));
            }
        }

        return variants;
    }

    private IntSet getAvailableIngredients(Set<AbstractVariant> pool) {
        pool.retainAll(variantToId.keySet());

        try {
            return ingredientCache.get(
                Set.copyOf(pool), () -> {
                    IntSet ingredients = new IntOpenHashSet();
                    ingredients.add(universalIngredientId);

                    for (AbstractVariant variant : pool) {
                        int id = variantToId.getInt(variant);
                        if (id >= 0) {
                            var ingredientIds = variantToIngredients.get(id);
                            if (ingredientIds != null) {
                                ingredients.addAll(ingredientIds);
                            }
                        }
                    }

                    return ingredients;
                }
            );
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Look up all recipes that can be made with (a subset of) the given pool of resources.
     *
     * @param pool the set of allowable variants. It will be modified to only contain known variants.
     * @return all recipes that can be made with the given pool of resources.
     */
    public List<R> lookup(Set<AbstractVariant> pool) {
        return trie.lookup(getAvailableIngredients(pool));
    }

    public static <R extends Recipe<?>> Builder<R> builder() {
        return new Builder<>();
    }

    public static class Builder<R extends Recipe<?>> {
        private final IntArrayTrie<R> trie = new IntArrayTrie<>();

        private final Map<Object, AbstractVariant> variantCache = new HashMap<>();
        private final Object2IntOpenHashMap<AbstractVariant> variantToId = new Object2IntOpenHashMap<>();
        private int nextVariantId = 0;

        private final Object2IntMap<AbstractIngredient> ingredientToId = new Object2IntOpenHashMap<>();
        private int nextIngredientId = 0;
        private final int universalIngredientId;

        private final Int2ObjectOpenHashMap<IntSet> variantToIngredients = new Int2ObjectOpenHashMap<>();

        private Builder() {
            variantToId.defaultReturnValue(-1);
            ingredientToId.defaultReturnValue(-1);

            universalIngredientId = getOrAssignId(AbstractIngredient.Universal.INSTANCE);
        }

        private int getOrAssignId(AbstractIngredient ingredient) {
            return ingredientToId.computeIfAbsent(
                ingredient, $ -> {
                    int id = nextIngredientId++;
                    for (AbstractVariant variant : ingredient.variants) {
                        variantToIngredients.computeIfAbsent(getOrAssignId(variant), $1 -> new IntOpenHashSet()).add(id);
                    }
                    return id;
                }
            );
        }

        private int getOrAssignId(AbstractVariant variant) {
            return variantToId.computeIfAbsent(variant, $ -> nextVariantId++);
        }

        private AbstractVariant getOrAssignVariant(Item item) {
            AbstractVariant variant = variantCache.computeIfAbsent(item, $ -> new AbstractVariant.AbstractItem(item));
            getOrAssignId(variant);
            return variant;
        }

        private AbstractVariant getOrAssignVariant(Fluid fluid) {
            AbstractVariant variant = variantCache.computeIfAbsent(fluid, $ -> new AbstractVariant.AbstractFluid(fluid));
            getOrAssignId(variant);
            return variant;
        }

        private void insert(AbstractRecipe<? extends R> recipe) {
            int[] key = new int[recipe.ingredients.size()];
            int i = 0;
            for (AbstractIngredient ingredient : recipe.ingredients) {
                key[i++] = getOrAssignId(ingredient);
            }
            Arrays.sort(key);
            trie.insert(key, recipe.recipe);
        }

        /**
         * Insert a recipe into the trie.
         * <br/>
         * Will handle item ingredients for all recipes, and fluid ingredients for {@link BasinRecipe}s.
         */
        public <R1 extends R> void insert(R1 recipe) {
            insert(createRecipe(recipe));
        }

        @SuppressWarnings("deprecation")
        private <R1 extends R> AbstractRecipe<R1> createRecipe(R1 recipe) {
            Set<AbstractIngredient> ingredients = new HashSet<>();

            Iterator<Ingredient> items = null;
            List<FluidIngredient> fluids = null;
            if (recipe instanceof ShapedRecipe shapedRecipe) {
                items = shapedRecipe.getIngredients().stream().filter(Optional::isPresent).map(Optional::get).iterator();
            } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                items = shapelessRecipe.ingredients.iterator();
            } else if (recipe instanceof BasinRecipe basinRecipe) {
                items = basinRecipe.getIngredients().stream().map(SizedIngredient::getIngredient).iterator();
                fluids = basinRecipe.getFluidIngredients();
            }
            if (items != null) {
                while (items.hasNext()) {
                    Ingredient ingredient = items.next();
                    if (ingredient.isEmpty()) {
                        ingredients.add(AbstractIngredient.Universal.INSTANCE);
                        continue;
                    }
                    Set<AbstractVariant> variants = new HashSet<>();
                    ingredient.items().forEach(entry -> variants.add(getOrAssignVariant(entry.value())));
                    ingredients.add(new AbstractIngredient(variants));
                }
            }

            if (fluids != null) {
                for (FluidIngredient ingredient : fluids) {
                    if (ingredient.amount() == 0) {
                        ingredients.add(AbstractIngredient.Universal.INSTANCE);
                        continue;
                    }
                    Set<AbstractVariant> variants = new HashSet<>();
                    for (Fluid fluid : ingredient.getMatchingFluids()) {
                        variants.add(getOrAssignVariant(fluid));
                    }
                    ingredients.add(new AbstractIngredient(variants));
                }
            }

            return new AbstractRecipe<>(recipe, ingredients);
        }

        public RecipeTrie<R> build() {
            variantToId.trim();
            variantToIngredients.trim();
            Create.LOGGER.info(
                "RecipeTrie of depth {} with {} nodes built with {} variants, {} ingredients, and {} recipes",
                trie.getMaxDepth(),
                trie.getNodeCount(),
                variantToId.size(),
                ingredientToId.size(),
                trie.getValueCount()
            );
            return new RecipeTrie<>(trie, variantToId, variantToIngredients, universalIngredientId);
        }
    }
}
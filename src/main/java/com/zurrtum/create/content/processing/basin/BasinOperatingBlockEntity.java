package com.zurrtum.create.content.processing.basin;

import com.zurrtum.create.Create;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.simple.DeferralBehaviour;
import com.zurrtum.create.foundation.recipe.RecipeFinder;
import com.zurrtum.create.foundation.recipe.trie.AbstractVariant;
import com.zurrtum.create.foundation.recipe.trie.RecipeTrie;
import com.zurrtum.create.foundation.recipe.trie.RecipeTrieFinder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public abstract class BasinOperatingBlockEntity extends KineticBlockEntity {

    public DeferralBehaviour basinChecker;
    public boolean basinRemoved;
    protected Recipe<?> currentRecipe;
    private final BasinRecipeFinder finder;

    public BasinOperatingBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        finder = new BasinRecipeFinder();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        basinChecker = new DeferralBehaviour(this, this::updateBasin);
        behaviours.add(basinChecker);
    }

    @Override
    public void onSpeedChanged(float prevSpeed) {
        super.onSpeedChanged(prevSpeed);
        basinRemoved = false;
        basinChecker.scheduleUpdate();
    }

    @Override
    public void tick() {
        if (basinRemoved) {
            basinRemoved = false;
            onBasinRemoved();
            sendData();
            return;
        }

        super.tick();
    }

    protected boolean updateBasin() {
        if (!isSpeedRequirementFulfilled())
            return true;
        if (getSpeed() == 0)
            return true;
        if (isRunning())
            return true;
        if (world == null || world.isClient())
            return true;
        Optional<BasinBlockEntity> basin = getBasin();
        if (basin.filter(BasinBlockEntity::canContinueProcessing).isEmpty())
            return true;

        Recipe<?> recipe = getMatchingRecipes();
        if (recipe == null)
            return true;
        currentRecipe = recipe;
        startProcessingBasin();
        sendData();
        return true;
    }

    protected abstract boolean isRunning();

    public void startProcessingBasin() {
    }

    public boolean continueWithPreviousRecipe() {
        return true;
    }

    protected boolean matchBasinRecipe(Recipe<?> recipe) {
        if (recipe == null)
            return false;
        return getBasin().map(blockEntity -> switch (recipe) {
            case BasinRecipe basinRecipe -> basinRecipe.matches(new BasinInput(blockEntity), world);
            case ShapedRecipe shapedRecipe -> BasinRecipe.matchCraftingRecipe(new BasinInput(blockEntity), shapedRecipe, world);
            case ShapelessRecipe shapelessRecipe -> BasinRecipe.matchCraftingRecipe(new BasinInput(blockEntity), shapelessRecipe, world);
            default -> false;
        }).orElse(false);

    }

    protected void applyBasinRecipe() {
        if (currentRecipe == null)
            return;

        Optional<BasinBlockEntity> optionalBasin = getBasin();
        if (optionalBasin.isEmpty())
            return;
        BasinBlockEntity basin = optionalBasin.get();
        boolean wasEmpty = basin.canContinueProcessing();
        switch (currentRecipe) {
            case BasinRecipe basinRecipe -> {
                if (!basinRecipe.apply(new BasinInput(basin)))
                    return;
            }
            case ShapedRecipe shapedRecipe -> {
                if (!BasinRecipe.applyCraftingRecipe(new BasinInput(basin), shapedRecipe, world))
                    return;
            }
            case ShapelessRecipe shapelessRecipe -> {
                if (!BasinRecipe.applyCraftingRecipe(new BasinInput(basin), shapelessRecipe, world))
                    return;
            }
            default -> {
                return;
            }
        }
        getProcessedRecipeTrigger().ifPresent(this::award);
        basin.inputTank.sendDataImmediately();

        // Continue mixing
        if (wasEmpty && matchBasinRecipe(currentRecipe)) {
            continueWithPreviousRecipe();
            sendData();
        }

        basin.notifyChangeOfContents();
    }

    protected Recipe<?> getMatchingRecipes() {
        Optional<BasinBlockEntity> $basin = getBasin();
        BasinBlockEntity basin;
        if ($basin.isEmpty() || (basin = $basin.get()).isEmpty())
            return null;
        if (basin.itemCapability == null && basin.fluidCapability == null) {
            return null;
        }
        try {
            RecipeTrie<Recipe<?>> trie = RecipeTrieFinder.get(getRecipeCacheKey(), (ServerWorld) world, this::matchStaticFilters);
            Set<AbstractVariant> availableVariants = RecipeTrie.getVariants(basin.itemCapability, basin.fluidCapability);
            return finder.match(basin, trie.lookup(availableVariants));
        } catch (Exception e) {
            Create.LOGGER.error("Failed to get recipe trie, falling back to slow logic", e);
            List<RecipeEntry<? extends Recipe<?>>> recipes = RecipeFinder.get(getRecipeCacheKey(), (ServerWorld) world, this::matchStaticFilters);
            if (recipes.isEmpty()) {
                return null;
            }
            return finder.matchEntry(basin, recipes);
        }
    }

    protected abstract void onBasinRemoved();

    protected Optional<BasinBlockEntity> getBasin() {
        if (world == null)
            return Optional.empty();
        BlockEntity basinBE = world.getBlockEntity(pos.down(2));
        if (!(basinBE instanceof BasinBlockEntity))
            return Optional.empty();
        return Optional.of((BasinBlockEntity) basinBE);
    }

    protected Optional<CreateTrigger> getProcessedRecipeTrigger() {
        return Optional.empty();
    }

    protected abstract boolean matchStaticFilters(RecipeEntry<? extends Recipe<?>> recipe);

    protected abstract Object getRecipeCacheKey();

    private class BasinRecipeFinder {
        private BasinInput basinInput;
        private Consumer<Recipe<?>> matchingStrategy;
        private Recipe<?> matchedRecipe;
        private int ingredientCount;

        public Recipe<?> match(BasinBlockEntity basin, List<Recipe<?>> recipes) {
            matchedRecipe = null;
            matchingStrategy = this::firstMatchStrategy;
            basinInput = new BasinInput(basin);
            for (Recipe<?> recipe : recipes) {
                matchingStrategy.accept(recipe);
            }
            return matchedRecipe;
        }

        public Recipe<?> matchEntry(BasinBlockEntity basin, List<RecipeEntry<? extends Recipe<?>>> recipes) {
            matchedRecipe = null;
            matchingStrategy = this::firstMatchStrategy;
            basinInput = new BasinInput(basin);
            for (RecipeEntry<? extends Recipe<?>> recipe : recipes) {
                matchingStrategy.accept(recipe.value());
            }
            return matchedRecipe;
        }

        private void updateMatchedRecipe(Recipe<?> recipe, int size) {
            matchedRecipe = recipe;
            ingredientCount = size;
        }

        private void firstMatchStrategy(Recipe<?> candidateRecipe) {
            switch (candidateRecipe) {
                case BasinRecipe recipe -> {
                    if (recipe.matches(basinInput, world)) {
                        updateMatchedRecipe(recipe, recipe.getIngredientSize());
                        matchingStrategy = this::selectBetterMatch;
                    }
                }
                case ShapedRecipe recipe -> {
                    if (BasinRecipe.matchCraftingRecipe(basinInput, recipe, world)) {
                        updateMatchedRecipe(recipe, (int) recipe.getIngredients().stream().filter(Optional::isPresent).count());
                        matchingStrategy = this::selectBetterMatch;
                    }
                }
                case ShapelessRecipe recipe -> {
                    if (BasinRecipe.matchCraftingRecipe(basinInput, recipe, world)) {
                        updateMatchedRecipe(recipe, recipe.ingredients.size());
                        matchingStrategy = this::selectBetterMatch;
                    }
                }
                default -> {
                }
            }
        }

        private void selectBetterMatch(Recipe<?> candidateRecipe) {
            switch (candidateRecipe) {
                case BasinRecipe recipe -> {
                    int count = recipe.getIngredientSize();
                    if (count > ingredientCount && recipe.matches(basinInput, world)) {
                        updateMatchedRecipe(recipe, count);
                    }
                }
                case ShapedRecipe recipe -> {
                    int count = recipe.getIngredients().size();
                    if (count > ingredientCount && BasinRecipe.matchCraftingRecipe(basinInput, recipe, world)) {
                        updateMatchedRecipe(recipe, count);
                    }
                }
                case ShapelessRecipe recipe -> {
                    int count = recipe.ingredients.size();
                    if (count > ingredientCount && BasinRecipe.matchCraftingRecipe(basinInput, recipe, world)) {
                        updateMatchedRecipe(recipe, count);
                    }
                }
                default -> {
                }
            }
        }
    }
}
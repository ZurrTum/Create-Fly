package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import com.zurrtum.create.content.kinetics.press.MechanicalPressBlockEntity;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class AutoCompactingDisplay extends CreateDisplay {
    public int size;
    public List<ItemStack> ingredient;
    public ItemStack result;

    public AutoCompactingDisplay() {
    }

    public AutoCompactingDisplay(ItemStack result, List<ItemStack> ingredient, int size) {
        this.result = result;
        this.ingredient = ingredient;
        this.size = size;
    }

    @Nullable
    public static AutoCompactingDisplay of(RecipeHolder<CraftingRecipe> entry) {
        CraftingRecipe recipe = entry.value();
        if (!MechanicalPressBlockEntity.canCompress(recipe) || AllRecipeTypes.shouldIgnoreInAutomation(entry)) {
            return null;
        }
        if (recipe instanceof ShapelessRecipe shapelessRecipe) {
            List<Ingredient> ingredients = shapelessRecipe.ingredients;
            return new AutoCompactingDisplay(shapelessRecipe.result, getItemStacks(ingredients.getFirst()), ingredients.size());
        } else if (recipe instanceof ShapedRecipe shapedRecipe) {
            List<ItemStack> input = null;
            int size = 0;
            for (Optional<Ingredient> value : shapedRecipe.getIngredients()) {
                if (value.isEmpty()) {
                    continue;
                }
                if (size == 0) {
                    input = getItemStacks(value.get());
                }
                size++;
            }
            return new AutoCompactingDisplay(shapedRecipe.result, input, size);
        }
        return null;
    }

    @Override
    public void writeToTag(CompoundTag tag) {
        RegistryOps<Tag> ops = getServerOps();
        tag.store("result", ItemStack.CODEC, ops, result);
        tag.store("ingredient", STACKS_CODEC, ops, ingredient);
        tag.putByte("size", (byte) size);
    }

    @Override
    public void loadFromTag(CompoundTag tag) {
        RegistryOps<Tag> ops = getClientOps();
        result = tag.read("result", ItemStack.CODEC, ops).orElseThrow();
        ingredient = tag.read("ingredient", STACKS_CODEC, ops).orElseThrow();
        size = tag.getByte("size").orElseThrow();
    }

    @Override
    public EivRecipeType<AutoCompactingDisplay> getRecipeType() {
        return EivCommonPlugin.AUTOMATIC_PACKING;
    }
}

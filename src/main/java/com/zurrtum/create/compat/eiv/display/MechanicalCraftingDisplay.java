package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryOps;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MechanicalCraftingDisplay extends CreateDisplay {
    public int width;
    public int height;
    public List<List<ItemStack>> ingredients;
    public int[] empty;
    public ItemStack result;

    public MechanicalCraftingDisplay() {
    }

    public MechanicalCraftingDisplay(RecipeEntry<MechanicalCraftingRecipe> entry) {
        MechanicalCraftingRecipe recipe = entry.value();
        RawShapedRecipe raw = recipe.raw();
        width = raw.getWidth();
        height = raw.getHeight();
        List<Optional<Ingredient>> ingredientList = raw.getIngredients();
        int size = ingredientList.size();
        ingredients = new ArrayList<>(size);
        int[] emptyList = new int[size];
        int actualSize = 0;
        int i = 0;
        for (; i < size; i++) {
            Optional<Ingredient> ingredient = ingredientList.get(i);
            if (ingredient.isEmpty()) {
                emptyList[actualSize++] = i;
                continue;
            }
            ingredients.add(getItemStacks(ingredient.get()));
        }
        empty = new int[actualSize];
        System.arraycopy(emptyList, 0, empty, 0, actualSize);
        result = recipe.result();
    }

    @Override
    public void writeToTag(NbtCompound tag) {
        RegistryOps<NbtElement> ops = getServerOps();
        tag.putByte("width", (byte) width);
        tag.putByte("height", (byte) height);
        tag.put("ingredients", STACKS_LIST_CODEC, ops, ingredients);
        tag.putIntArray("empty", empty);
        tag.put("result", ItemStack.CODEC, ops, result);
    }

    @Override
    public void loadFromTag(NbtCompound tag) {
        RegistryOps<NbtElement> ops = getClientOps();
        width = tag.getByte("width").orElseThrow();
        height = tag.getByte("height").orElseThrow();
        ingredients = tag.get("ingredients", STACKS_LIST_CODEC, ops).orElseThrow();
        empty = tag.getIntArray("empty").orElseThrow();
        result = tag.get("result", ItemStack.CODEC, ops).orElseThrow();
    }

    @Override
    public EivRecipeType<MechanicalCraftingDisplay> getRecipeType() {
        return EivCommonPlugin.MECHANICAL_CRAFTING;
    }
}

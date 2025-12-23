package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

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

    public MechanicalCraftingDisplay(RecipeHolder<MechanicalCraftingRecipe> entry) {
        MechanicalCraftingRecipe recipe = entry.value();
        ShapedRecipePattern raw = recipe.raw();
        width = raw.width();
        height = raw.height();
        List<Optional<Ingredient>> ingredientList = raw.ingredients();
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
    public void writeToTag(CompoundTag tag) {
        RegistryOps<Tag> ops = getServerOps();
        tag.putByte("width", (byte) width);
        tag.putByte("height", (byte) height);
        tag.store("ingredients", STACKS_LIST_CODEC, ops, ingredients);
        tag.putIntArray("empty", empty);
        tag.store("result", ItemStack.CODEC, ops, result);
    }

    @Override
    public void loadFromTag(CompoundTag tag) {
        RegistryOps<Tag> ops = getClientOps();
        width = tag.getByte("width").orElseThrow();
        height = tag.getByte("height").orElseThrow();
        ingredients = tag.read("ingredients", STACKS_LIST_CODEC, ops).orElseThrow();
        empty = tag.getIntArray("empty").orElseThrow();
        result = tag.read("result", ItemStack.CODEC, ops).orElseThrow();
    }

    @Override
    public EivRecipeType<MechanicalCraftingDisplay> getRecipeType() {
        return EivCommonPlugin.MECHANICAL_CRAFTING;
    }
}

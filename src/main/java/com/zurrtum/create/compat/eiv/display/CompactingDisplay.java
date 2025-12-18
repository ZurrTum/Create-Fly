package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import com.zurrtum.create.content.kinetics.mixer.CompactingRecipe;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryOps;

import java.util.ArrayList;
import java.util.List;

public class CompactingDisplay extends CreateDisplay {
    public ItemStack result;
    public List<List<ItemStack>> ingredients;

    public CompactingDisplay() {
    }

    public CompactingDisplay(RecipeEntry<CompactingRecipe> entry) {
        CompactingRecipe recipe = entry.value();
        result = recipe.result();
        ingredients = new ArrayList<>(recipe.getIngredientSize());
        addSizedIngredient(recipe.ingredients(), ingredients);
        FluidIngredient fluidIngredient = recipe.fluidIngredient();
        if (fluidIngredient != null) {
            ingredients.add(getItemStacks(fluidIngredient));
        }
    }

    @Override
    public void writeToTag(NbtCompound tag) {
        RegistryOps<NbtElement> ops = getServerOps();
        tag.put("result", ItemStack.CODEC, ops, result);
        tag.put("ingredients", STACKS_LIST_CODEC, ops, ingredients);
    }

    @Override
    public void loadFromTag(NbtCompound tag) {
        RegistryOps<NbtElement> ops = getClientOps();
        result = tag.get("result", ItemStack.CODEC, ops).orElseThrow();
        ingredients = tag.get("ingredients", STACKS_LIST_CODEC, ops).orElseThrow();
    }

    @Override
    public EivRecipeType<CompactingDisplay> getRecipeType() {
        return EivCommonPlugin.PACKING;
    }
}

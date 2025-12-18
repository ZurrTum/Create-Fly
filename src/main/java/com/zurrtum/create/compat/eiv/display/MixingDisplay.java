package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryOps;

import java.util.ArrayList;
import java.util.List;

public class MixingDisplay extends CreateDisplay {
    public ItemStack result;
    public List<List<ItemStack>> ingredients;
    public HeatCondition heat;

    public MixingDisplay() {
    }

    public MixingDisplay(RecipeEntry<MixingRecipe> entry) {
        MixingRecipe recipe = entry.value();
        result = recipe.result();
        if (result.isEmpty()) {
            result = getItemStack(recipe.fluidResult());
        }
        ingredients = new ArrayList<>(recipe.getIngredientSize());
        addSizedIngredient(recipe.ingredients(), ingredients);
        for (FluidIngredient ingredient : recipe.fluidIngredients()) {
            ingredients.add(getItemStacks(ingredient));
        }
        heat = recipe.heat();
    }

    @Override
    public void writeToTag(NbtCompound tag) {
        RegistryOps<NbtElement> ops = getServerOps();
        tag.put("result", ItemStack.CODEC, ops, result);
        tag.put("ingredients", STACKS_LIST_CODEC, ops, ingredients);
        if (heat != HeatCondition.NONE) {
            tag.put("heat", HeatCondition.CODEC, ops, heat);
        }
    }

    @Override
    public void loadFromTag(NbtCompound tag) {
        RegistryOps<NbtElement> ops = getClientOps();
        result = tag.get("result", ItemStack.CODEC, ops).orElseThrow();
        ingredients = tag.get("ingredients", STACKS_LIST_CODEC, ops).orElseThrow();
        heat = tag.get("heat", HeatCondition.CODEC, ops).orElse(HeatCondition.NONE);
    }

    @Override
    public EivRecipeType<MixingDisplay> getRecipeType() {
        return EivCommonPlugin.MIXING;
    }
}

package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import com.zurrtum.create.content.kinetics.crusher.AbstractCrushingRecipe;
import com.zurrtum.create.content.processing.recipe.ChanceOutput;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import de.crafty.eiv.common.api.recipe.IEivServerRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryOps;

import java.util.ArrayList;
import java.util.List;

public class CrushingDisplay extends CreateDisplay {
    public List<ItemStack> results;
    public List<Float> chances;
    public List<ItemStack> ingredient;

    public CrushingDisplay() {
    }

    public CrushingDisplay(RecipeEntry<? extends AbstractCrushingRecipe> entry) {
        AbstractCrushingRecipe recipe = entry.value();
        int size = recipe.results().size();
        results = new ArrayList<>(size);
        chances = new ArrayList<>(size);
        for (ChanceOutput output : recipe.results()) {
            results.add(output.stack());
            chances.add(output.chance());
        }
        ingredient = getItemStacks(recipe.ingredient());
    }

    @Override
    public void writeToTag(NbtCompound tag) {
        RegistryOps<NbtElement> ops = getServerOps();
        tag.put("results", STACKS_CODEC, ops, results);
        tag.put("chances", CreateCodecs.FLOAT_LIST_CODEC, ops, chances);
        tag.put("ingredient", STACKS_CODEC, ops, ingredient);
    }

    @Override
    public void loadFromTag(NbtCompound tag) {
        RegistryOps<NbtElement> ops = getClientOps();
        results = tag.get("results", STACKS_CODEC, ops).orElseThrow();
        chances = tag.get("chances", CreateCodecs.FLOAT_LIST_CODEC, ops).orElseThrow();
        ingredient = tag.get("ingredient", STACKS_CODEC, ops).orElseThrow();
    }

    @Override
    public EivRecipeType<? extends IEivServerRecipe> getRecipeType() {
        return EivCommonPlugin.CRUSHING;
    }
}

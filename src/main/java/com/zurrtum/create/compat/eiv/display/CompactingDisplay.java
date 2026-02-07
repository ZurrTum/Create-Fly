package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import com.zurrtum.create.content.kinetics.mixer.CompactingRecipe;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.codec.CreateCodecs;
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
    public List<ItemStack> results;
    public List<Float> chances;
    public List<List<ItemStack>> ingredients;
    public List<FluidIngredient> fluidIngredients;
    public HeatCondition heat;

    public CompactingDisplay() {
    }

    public CompactingDisplay(RecipeEntry<CompactingRecipe> entry) {
        CompactingRecipe recipe = entry.value();
        List<ProcessingOutput> outputs = recipe.results();
        int size = outputs.size();
        results = new ArrayList<>(size);
        chances = new ArrayList<>(size);
        for (ProcessingOutput output : outputs) {
            results.add(output.create());
            chances.add(output.chance());
        }
        ingredients = new ArrayList<>(recipe.ingredients().size());
        addSizedIngredient(recipe.ingredients(), ingredients);
        fluidIngredients = recipe.fluidIngredients();
        heat = recipe.heat();
    }

    @Override
    public void writeToTag(NbtCompound tag) {
        RegistryOps<NbtElement> ops = getServerOps();
        tag.put("results", STACKS_CODEC, ops, results);
        tag.put("chances", CreateCodecs.FLOAT_LIST_CODEC, ops, chances);
        tag.put("ingredients", STACKS_LIST_CODEC, ops, ingredients);
        if (!fluidIngredients.isEmpty()) {
            tag.put("fluidIngredients", FLUID_INGREDIENTS_CODEC, ops, fluidIngredients);
        }
        if (heat != HeatCondition.NONE) {
            tag.put("heat", HeatCondition.CODEC, ops, heat);
        }
    }

    @Override
    public void loadFromTag(NbtCompound tag) {
        RegistryOps<NbtElement> ops = getClientOps();
        results = tag.get("results", STACKS_CODEC, ops).orElseThrow();
        chances = tag.get("chances", CreateCodecs.FLOAT_LIST_CODEC, ops).orElseThrow();
        ingredients = tag.get("ingredients", STACKS_LIST_CODEC, ops).orElseThrow();
        fluidIngredients = tag.get("fluidIngredients", FLUID_INGREDIENTS_CODEC, ops).orElse(List.of());
        heat = tag.get("heat", HeatCondition.CODEC, ops).orElse(HeatCondition.NONE);
    }

    @Override
    public EivRecipeType<CompactingDisplay> getRecipeType() {
        return EivCommonPlugin.PACKING;
    }
}

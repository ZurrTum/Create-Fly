package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

public class MixingDisplay extends CreateDisplay {
    public List<ItemStack> results;
    public List<Float> chances;
    public List<FluidStack> fluidResults;
    public List<List<ItemStack>> ingredients;
    public List<FluidIngredient> fluidIngredients;
    public HeatCondition heat;

    public MixingDisplay() {
    }

    public MixingDisplay(RecipeHolder<MixingRecipe> entry) {
        MixingRecipe recipe = entry.value();
        List<ProcessingOutput> outputs = recipe.results();
        int size = outputs.size();
        results = new ArrayList<>(size);
        chances = new ArrayList<>(size);
        for (ProcessingOutput output : outputs) {
            results.add(output.create());
            chances.add(output.chance());
        }
        fluidResults = recipe.fluidResults();
        ingredients = new ArrayList<>(recipe.ingredients().size());
        addSizedIngredient(recipe.ingredients(), ingredients);
        fluidIngredients = recipe.fluidIngredients();
        heat = recipe.heat();
    }

    @Override
    public void writeToTag(CompoundTag tag) {
        RegistryOps<Tag> ops = getServerOps();
        tag.store("results", STACKS_CODEC, ops, results);
        tag.store("chances", CreateCodecs.FLOAT_LIST_CODEC, ops, chances);
        if (!fluidResults.isEmpty()) {
            tag.store("fluidResults", FLUID_STACKS_CODEC, fluidResults);
        }
        tag.store("ingredients", STACKS_LIST_CODEC, ops, ingredients);
        if (!fluidIngredients.isEmpty()) {
            tag.store("fluidIngredients", FLUID_INGREDIENTS_CODEC, ops, fluidIngredients);
        }
        if (heat != HeatCondition.NONE) {
            tag.store("heat", HeatCondition.CODEC, ops, heat);
        }
    }

    @Override
    public void loadFromTag(CompoundTag tag) {
        RegistryOps<Tag> ops = getClientOps();
        results = tag.read("results", STACKS_CODEC, ops).orElseThrow();
        chances = tag.read("chances", CreateCodecs.FLOAT_LIST_CODEC, ops).orElseThrow();
        fluidResults = tag.read("fluidResults", FLUID_STACKS_CODEC, ops).orElse(List.of());
        fluidIngredients = tag.read("fluidIngredients", FLUID_INGREDIENTS_CODEC, ops).orElse(List.of());
        ingredients = tag.read("ingredients", STACKS_LIST_CODEC, ops).orElseThrow();
        heat = tag.read("heat", HeatCondition.CODEC, ops).orElse(HeatCondition.NONE);
    }

    @Override
    public EivRecipeType<MixingDisplay> getRecipeType() {
        return EivCommonPlugin.MIXING;
    }
}

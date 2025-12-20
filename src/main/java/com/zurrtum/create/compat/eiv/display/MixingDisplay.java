package com.zurrtum.create.compat.eiv.display;

import com.mojang.serialization.Codec;
import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryOps;

import java.util.ArrayList;
import java.util.List;

public class MixingDisplay extends CreateDisplay {
    private static final Codec<List<FluidIngredient>> FLUID_INGREDIENTS_CODEC = FluidIngredient.CODEC.listOf();
    public ItemStack result;
    public FluidStack fluidResult;
    public List<List<ItemStack>> ingredients;
    public List<FluidIngredient> fluidIngredients;
    public HeatCondition heat;

    public MixingDisplay() {
    }

    public MixingDisplay(RecipeEntry<MixingRecipe> entry) {
        MixingRecipe recipe = entry.value();
        result = recipe.result();
        fluidResult = recipe.fluidResult();
        ingredients = new ArrayList<>(recipe.ingredients().size());
        addSizedIngredient(recipe.ingredients(), ingredients);
        fluidIngredients = recipe.fluidIngredients();
        heat = recipe.heat();
    }

    @Override
    public void writeToTag(NbtCompound tag) {
        RegistryOps<NbtElement> ops = getServerOps();
        if (result.isEmpty()) {
            tag.put("fluidResult", FluidStack.CODEC, fluidResult);
        } else {
            tag.put("result", ItemStack.CODEC, ops, result);
        }
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
        result = tag.get("result", ItemStack.CODEC, ops).orElse(ItemStack.EMPTY);
        fluidResult = tag.get("fluidResult", FluidStack.CODEC, ops).orElse(FluidStack.EMPTY);
        fluidIngredients = tag.get("fluidIngredients", FLUID_INGREDIENTS_CODEC, ops).orElse(List.of());
        ingredients = tag.get("ingredients", STACKS_LIST_CODEC, ops).orElseThrow();
        heat = tag.get("heat", HeatCondition.CODEC, ops).orElse(HeatCondition.NONE);
    }

    @Override
    public EivRecipeType<MixingDisplay> getRecipeType() {
        return EivCommonPlugin.MIXING;
    }
}

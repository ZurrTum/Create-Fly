package com.zurrtum.create.compat.eiv.display;

import com.mojang.serialization.Codec;
import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
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
    private static final Codec<List<FluidIngredient>> FLUID_INGREDIENTS_CODEC = FluidIngredient.CODEC.listOf();
    public ItemStack result;
    public FluidStack fluidResult;
    public List<List<ItemStack>> ingredients;
    public List<FluidIngredient> fluidIngredients;
    public HeatCondition heat;

    public MixingDisplay() {
    }

    public MixingDisplay(RecipeHolder<MixingRecipe> entry) {
        MixingRecipe recipe = entry.value();
        result = recipe.result();
        fluidResult = recipe.fluidResult();
        ingredients = new ArrayList<>(recipe.ingredients().size());
        addSizedIngredient(recipe.ingredients(), ingredients);
        fluidIngredients = recipe.fluidIngredients();
        heat = recipe.heat();
    }

    @Override
    public void writeToTag(CompoundTag tag) {
        RegistryOps<Tag> ops = getServerOps();
        if (result.isEmpty()) {
            tag.store("fluidResult", FluidStack.CODEC, fluidResult);
        } else {
            tag.store("result", ItemStack.CODEC, ops, result);
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
        result = tag.read("result", ItemStack.CODEC, ops).orElse(ItemStack.EMPTY);
        fluidResult = tag.read("fluidResult", FluidStack.CODEC, ops).orElse(FluidStack.EMPTY);
        fluidIngredients = tag.read("fluidIngredients", FLUID_INGREDIENTS_CODEC, ops).orElse(List.of());
        ingredients = tag.read("ingredients", STACKS_LIST_CODEC, ops).orElseThrow();
        heat = tag.read("heat", HeatCondition.CODEC, ops).orElse(HeatCondition.NONE);
    }

    @Override
    public EivRecipeType<MixingDisplay> getRecipeType() {
        return EivCommonPlugin.MIXING;
    }
}

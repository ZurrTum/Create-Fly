package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import com.zurrtum.create.content.kinetics.mixer.PotionRecipe;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryOps;

import java.util.List;

public class PotionDisplay extends CreateDisplay {
    public FluidStack result;
    public List<ItemStack> ingredient;
    public FluidIngredient fluidIngredient;

    public PotionDisplay() {
    }

    public PotionDisplay(RecipeEntry<PotionRecipe> entry) {
        PotionRecipe recipe = entry.value();
        result = recipe.result();
        ingredient = getItemStacks(recipe.ingredient());
        fluidIngredient = recipe.fluidIngredient();
    }

    @Override
    public void writeToTag(NbtCompound tag) {
        RegistryOps<NbtElement> ops = getServerOps();
        tag.put("result", FluidStack.CODEC, ops, result);
        tag.put("ingredient", STACKS_CODEC, ops, ingredient);
        tag.put("fluidIngredient", FluidIngredient.CODEC, ops, fluidIngredient);
    }

    @Override
    public void loadFromTag(NbtCompound tag) {
        RegistryOps<NbtElement> ops = getClientOps();
        result = tag.get("result", FluidStack.CODEC, ops).orElseThrow();
        ingredient = tag.get("ingredient", STACKS_CODEC, ops).orElseThrow();
        fluidIngredient = tag.get("fluidIngredient", FluidIngredient.CODEC, ops).orElseThrow();
    }

    @Override
    public EivRecipeType<PotionDisplay> getRecipeType() {
        return EivCommonPlugin.AUTOMATIC_BREWING;
    }
}

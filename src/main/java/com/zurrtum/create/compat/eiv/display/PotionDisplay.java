package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import com.zurrtum.create.content.kinetics.mixer.PotionRecipe;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class PotionDisplay extends CreateDisplay {
    public FluidStack result;
    public List<ItemStack> ingredient;
    public FluidIngredient fluidIngredient;

    public PotionDisplay() {
    }

    public PotionDisplay(RecipeHolder<PotionRecipe> entry) {
        PotionRecipe recipe = entry.value();
        result = recipe.result();
        ingredient = getItemStacks(recipe.ingredient());
        fluidIngredient = recipe.fluidIngredient();
    }

    @Override
    public void writeToTag(CompoundTag tag) {
        RegistryOps<Tag> ops = getServerOps();
        tag.store("result", FluidStack.CODEC, ops, result);
        tag.store("ingredient", STACKS_CODEC, ops, ingredient);
        tag.store("fluidIngredient", FluidIngredient.CODEC, ops, fluidIngredient);
    }

    @Override
    public void loadFromTag(CompoundTag tag) {
        RegistryOps<Tag> ops = getClientOps();
        result = tag.read("result", FluidStack.CODEC, ops).orElseThrow();
        ingredient = tag.read("ingredient", STACKS_CODEC, ops).orElseThrow();
        fluidIngredient = tag.read("fluidIngredient", FluidIngredient.CODEC, ops).orElseThrow();
    }

    @Override
    public EivRecipeType<PotionDisplay> getRecipeType() {
        return EivCommonPlugin.AUTOMATIC_BREWING;
    }
}

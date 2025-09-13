package com.zurrtum.create.content.fluids.spout;

import com.zurrtum.create.AllRecipeSets;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.content.fluids.transfer.FillingInput;
import com.zurrtum.create.content.fluids.transfer.FillingRecipe;
import com.zurrtum.create.content.fluids.transfer.GenericItemFilling;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.Optional;

public class FillingBySpout {
    public static boolean canItemBeFilled(World world, ItemStack stack) {
        if (world.getRecipeManager().getPropertySet(AllRecipeSets.FILLING).canUse(stack)) {
            return true;
        }
        return GenericItemFilling.canItemBeFilled(world, stack);
    }

    public static int getRequiredAmountForItem(ServerWorld world, ItemStack stack, FluidStack availableFluid) {
        FillingInput input = new FillingInput(stack, availableFluid);
        Optional<RecipeEntry<FillingRecipe>> findRecipe = world.getRecipeManager().getFirstMatch(AllRecipeTypes.FILLING, input, world);
        return findRecipe.map(fillingRecipeRecipeEntry -> fillingRecipeRecipeEntry.value().fluidIngredient().amount())
            .orElseGet(() -> GenericItemFilling.getRequiredAmountForItem(world, stack, availableFluid));
    }

    public static ItemStack fillItem(ServerWorld world, int requiredAmount, ItemStack stack, FluidStack availableFluid) {
        FluidStack toFill = availableFluid.copy();
        toFill.setAmount(requiredAmount);
        FillingInput input = new FillingInput(stack, toFill);
        Optional<RecipeEntry<FillingRecipe>> findRecipe = world.getRecipeManager().getFirstMatch(AllRecipeTypes.FILLING, input, world);
        if (findRecipe.isPresent()) {
            FillingRecipe recipe = findRecipe.get().value();
            ItemStack result = recipe.craft(input, world.getRegistryManager());
            availableFluid.decrement(recipe.fluidIngredient().amount());
            stack.decrement(1);
            return result;
        }

        return GenericItemFilling.fillItem(world, requiredAmount, stack, availableFluid);
    }
}

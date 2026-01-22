package com.zurrtum.create.content.fluids.spout;

import com.zurrtum.create.AllRecipeSets;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.content.fluids.transfer.FillingInput;
import com.zurrtum.create.content.fluids.transfer.FillingRecipe;
import com.zurrtum.create.content.fluids.transfer.GenericItemFilling;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class FillingBySpout {
    public static boolean canItemBeFilled(Level world, ItemStack stack) {
        if (world.recipeAccess().propertySet(AllRecipeSets.FILLING).test(stack)) {
            return true;
        }
        return GenericItemFilling.canItemBeFilled(world, stack);
    }

    public static int getRequiredAmountForItem(ServerLevel world, ItemStack stack, FluidStack availableFluid) {
        FillingInput input = new FillingInput(stack, availableFluid);
        Optional<RecipeHolder<FillingRecipe>> findRecipe = world.recipeAccess().getRecipeFor(AllRecipeTypes.FILLING, input, world);
        return findRecipe.map(fillingRecipeRecipeEntry -> fillingRecipeRecipeEntry.value().fluidIngredient().amount())
            .orElseGet(() -> GenericItemFilling.getRequiredAmountForItem(world, stack, availableFluid));
    }

    public static ItemStack fillItem(ServerLevel world, int requiredAmount, ItemStack stack, FluidStack availableFluid) {
        FluidStack toFill = availableFluid.copy();
        toFill.setAmount(requiredAmount);
        FillingInput input = new FillingInput(stack, toFill);
        Optional<RecipeHolder<FillingRecipe>> findRecipe = world.recipeAccess().getRecipeFor(AllRecipeTypes.FILLING, input, world);
        if (findRecipe.isPresent()) {
            FillingRecipe recipe = findRecipe.get().value();
            ItemStack result = recipe.assemble(input);
            availableFluid.decrement(recipe.fluidIngredient().amount());
            stack.shrink(1);
            return result;
        }

        return GenericItemFilling.fillItem(world, requiredAmount, stack, availableFluid);
    }
}

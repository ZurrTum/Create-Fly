package com.zurrtum.create.content.fluids.transfer;

import com.zurrtum.create.AllRecipeSets;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidItemInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.Optional;

public class GenericItemEmptying {

    public static boolean canItemBeEmptied(World world, ItemStack stack) {
        if (PotionFluidHandler.isPotionItem(stack))
            return true;

        if (world.isClient ? world.getRecipeManager().getPropertySet(AllRecipeSets.EMPTYING).canUse(stack) : ((ServerWorld) world).getRecipeManager()
            .getFirstMatch(AllRecipeTypes.EMPTYING, new SingleStackRecipeInput(stack), world).isPresent()) {
            return true;
        }

        try (FluidItemInventory capability = FluidHelper.getFluidInventory(stack)) {
            if (capability == null) {
                return false;
            }
            return capability.stream().anyMatch(fluidStack -> fluidStack.getAmount() > 0);
        }
    }

    public static Pair<FluidStack, ItemStack> emptyItem(World world, ItemStack stack, boolean simulate) {
        if (PotionFluidHandler.isPotionItem(stack))
            return PotionFluidHandler.emptyPotion(stack, simulate);

        //TODO client check recipe
        if (!world.isClient) {
            Optional<RecipeEntry<EmptyingRecipe>> recipe = ((ServerWorld) world).getRecipeManager()
                .getFirstMatch(AllRecipeTypes.EMPTYING, new SingleStackRecipeInput(stack), world);
            if (recipe.isPresent()) {
                if (!simulate)
                    stack.decrement(1);
                EmptyingRecipe emptyingRecipe = recipe.get().value();
                return Pair.of(emptyingRecipe.fluidResult(), emptyingRecipe.result());
            }
        } else {
            Create.LOGGER.warn("Client check recipe " + stack.getName().getString());
        }

        try (FluidItemInventory capability = FluidHelper.getFluidInventory(stack.copyWithCount(1))) {
            if (capability == null)
                return Pair.of(FluidStack.EMPTY, ItemStack.EMPTY);
            Optional<FluidStack> findFluid = capability.stream().filter(fluid -> fluid.getAmount() >= BucketFluidInventory.CAPACITY).findFirst();
            if (findFluid.isEmpty()) {
                return Pair.of(FluidStack.EMPTY, ItemStack.EMPTY);
            }
            FluidStack resultingFluid = findFluid.get();
            capability.extract(resultingFluid);
            if (!simulate)
                stack.decrement(1);

            return Pair.of(resultingFluid, capability.getContainer());
        }
    }

}

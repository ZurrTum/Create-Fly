package com.zurrtum.create.content.processing.basin;

import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.util.math.random.Random;

import java.util.List;

public record BasinInput(
    ServerFilteringBehaviour filter, HeatLevel heat, FluidInventory fluids, Inventory items, Random random,
    BasinBlockEntity blockEntity
) implements RecipeInput {
    public BasinInput(BasinBlockEntity basin) {
        this(basin.getFilter(), basin.getHeatLevel(), basin.fluidCapability, basin.itemCapability, basin.getWorld().getRandom(), basin);
    }

    public boolean acceptOutputs(List<ItemStack> outputItems, List<FluidStack> outputFluids, boolean simulate) {
        return blockEntity.acceptOutputs(outputItems, outputFluids, simulate);
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}

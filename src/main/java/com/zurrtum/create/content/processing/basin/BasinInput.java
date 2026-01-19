package com.zurrtum.create.content.processing.basin;

import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;

public record BasinInput(
    ServerFilteringBehaviour filter, HeatLevel heat, FluidInventory fluids, Container items, BasinBlockEntity blockEntity
) implements RecipeInput {
    public BasinInput(BasinBlockEntity basin) {
        this(basin.getFilter(), basin.getHeatLevel(), basin.fluidCapability, basin.itemCapability, basin);
    }

    public boolean acceptOutputs(List<ItemStack> outputItems, List<FluidStack> outputFluids, boolean simulate) {
        return blockEntity.acceptOutputs(outputItems, outputFluids, simulate);
    }

    @Override
    public ItemStack getItem(int slot) {
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

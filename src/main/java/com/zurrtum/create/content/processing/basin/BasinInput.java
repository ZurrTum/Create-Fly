package com.zurrtum.create.content.processing.basin;

import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import org.apache.commons.lang3.function.TriFunction;

import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record BasinInput(
    ServerFilteringBehaviour filter, HeatLevel heat, FluidInventory fluids, Container items,
    TriFunction<List<ItemStack>, List<FluidStack>, Boolean, Boolean> callback
) implements RecipeInput {
    public BasinInput(BasinBlockEntity basin) {
        this(basin.getFilter(), basin.getHeatLevel(), basin.fluidCapability, basin.itemCapability, basin::acceptOutputs);
    }

    public boolean acceptOutputs(List<ItemStack> outputItems, List<FluidStack> outputFluids, boolean simulate) {
        return callback.apply(outputItems, outputFluids, simulate);
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

package com.zurrtum.create.content.fluids.transfer;

import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;

public record FillingInput(ItemStack item, FluidStack fluid) implements RecipeInput {
    @Override
    public ItemStack getStackInSlot(int slot) {
        return switch (slot) {
            case 0 -> item;
            case 1 -> ItemStack.EMPTY;
            default -> throw new IllegalArgumentException("Unexpected value: " + slot);
        };
    }

    @Override
    public int size() {
        return 2;
    }
}

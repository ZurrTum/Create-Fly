package com.zurrtum.create.content.kinetics.deployer;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;

public record ItemApplicationInput(ItemStack target, ItemStack ingredient) implements RecipeInput {
    @Override
    public ItemStack getStackInSlot(int slot) {
        return switch (slot) {
            case 0 -> target;
            case 1 -> ingredient;
            default -> throw new IllegalArgumentException("Unexpected value: " + slot);
        };
    }

    @Override
    public int size() {
        return 2;
    }
}

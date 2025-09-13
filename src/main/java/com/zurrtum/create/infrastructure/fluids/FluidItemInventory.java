package com.zurrtum.create.infrastructure.fluids;

import net.minecraft.item.ItemStack;

public interface FluidItemInventory extends FluidInventory, AutoCloseable {
    ItemStack getContainer();

    void close();
}

package com.zurrtum.create.infrastructure.fluids;

import net.minecraft.world.item.ItemStack;

public interface FluidItemInventory extends FluidInventory, AutoCloseable {
    ItemStack getContainer();

    void close();
}

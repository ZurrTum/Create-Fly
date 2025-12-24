package com.zurrtum.create.compat.computercraft.events;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RepackageEvent implements ComputerEvent {

    public @NotNull ItemStack box;
    public int count;

    public RepackageEvent(@NotNull ItemStack box, int count) {
        this.box = box;
        this.count = count;
    }

}

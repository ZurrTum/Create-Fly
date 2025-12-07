package com.zurrtum.create.compat.computercraft.events;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PackageEvent implements ComputerEvent {

    public @NotNull ItemStack box;
    public String status;

    public PackageEvent(@NotNull ItemStack box, String status) {
        this.box = box;
        this.status = status;
    }

}

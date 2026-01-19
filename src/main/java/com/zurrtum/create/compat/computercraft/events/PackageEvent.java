package com.zurrtum.create.compat.computercraft.events;

import net.minecraft.world.item.ItemStack;

public class PackageEvent implements ComputerEvent {

    public ItemStack box;
    public String status;

    public PackageEvent(ItemStack box, String status) {
        this.box = box;
        this.status = status;
    }

}

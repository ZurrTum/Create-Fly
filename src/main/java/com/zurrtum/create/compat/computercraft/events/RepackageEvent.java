package com.zurrtum.create.compat.computercraft.events;

import net.minecraft.world.item.ItemStack;

public class RepackageEvent implements ComputerEvent {

    public ItemStack box;
    public int count;

    public RepackageEvent(ItemStack box, int count) {
        this.box = box;
        this.count = count;
    }

}

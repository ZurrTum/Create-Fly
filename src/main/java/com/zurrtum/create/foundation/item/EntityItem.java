package com.zurrtum.create.foundation.item;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface EntityItem {
    Entity createEntity(World world, Entity location, ItemStack itemstack);
}

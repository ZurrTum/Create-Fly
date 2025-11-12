package com.zurrtum.create.foundation.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface EntityItem {
    Entity createEntity(Level world, Entity location, ItemStack itemstack);
}

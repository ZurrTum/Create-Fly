package com.zurrtum.create.foundation.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public interface SwingControlItem {
    boolean onEntitySwing(ItemStack stack, LivingEntity entity, Hand hand);
}

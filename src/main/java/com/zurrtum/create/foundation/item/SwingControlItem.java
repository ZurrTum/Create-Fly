package com.zurrtum.create.foundation.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface SwingControlItem {
    boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand);
}

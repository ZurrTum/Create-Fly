package com.zurrtum.create.content.equipment;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;

public class BuildersTeaItem extends Item {
    public BuildersTeaItem(Settings properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World level, LivingEntity livingEntity) {
        ItemStack eatResult = super.finishUsing(stack, level, livingEntity);
        if (livingEntity instanceof PlayerEntity player && !player.getAbilities().creativeMode) {
            if (eatResult.isEmpty()) {
                return Items.GLASS_BOTTLE.getDefaultStack();
            } else {
                player.getInventory().insertStack(Items.GLASS_BOTTLE.getDefaultStack());
            }
        }
        return eatResult;
    }
}

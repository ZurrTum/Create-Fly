package com.zurrtum.create.content.processing.sequenced;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.catnip.theme.Color;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SequencedAssemblyItem extends Item {

    public SequencedAssemblyItem(Properties settings) {
        super(settings);
    }

    public float getProgress(ItemStack stack) {
        Float process = stack.get(AllDataComponents.SEQUENCED_ASSEMBLY_PROGRESS);
        return process != null ? process : 0;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(getProgress(stack) * 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Color.mixColors(0xFF_FFC074, 0xFF_46FFE0, getProgress(stack));
    }

}

package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.zurrtum.create.content.redstone.smartObserver.SmartObserverBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class ItemCountDisplaySource extends NumericSingleLineDisplaySource {
    @Override
    protected MutableText provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        BlockEntity sourceBE = context.getSourceBlockEntity();
        if (!(sourceBE instanceof SmartObserverBlockEntity cobe))
            return ZERO.copy();

        InvManipulationBehaviour invManipulationBehaviour = cobe.getBehaviour(InvManipulationBehaviour.TYPE);
        ServerFilteringBehaviour filteringBehaviour = cobe.getBehaviour(ServerFilteringBehaviour.TYPE);
        Inventory handler = invManipulationBehaviour.getInventory();

        if (handler == null)
            return ZERO.copy();

        int collected = 0;
        for (int i = 0, size = handler.size(); i < size; i++) {
            ItemStack stack = handler.getStack(i);
            if (stack.isEmpty())
                continue;
            if (filteringBehaviour.test(stack))
                collected += stack.getCount();
        }

        return Text.literal(String.valueOf(collected));
    }

    @Override
    protected String getTranslationKey() {
        return "count_items";
    }

    @Override
    public boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}
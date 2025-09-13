package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.catnip.data.IntAttached;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.smartObserver.SmartObserverBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.zurrtum.create.foundation.item.CountedItemStackList;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.text.MutableText;

import java.util.stream.Stream;

public class ItemListDisplaySource extends ValueListDisplaySource {

    @Override
    protected Stream<IntAttached<MutableText>> provideEntries(DisplayLinkContext context, int maxRows) {
        BlockEntity sourceBE = context.getSourceBlockEntity();
        if (!(sourceBE instanceof SmartObserverBlockEntity cobe))
            return Stream.empty();

        InvManipulationBehaviour invManipulationBehaviour = cobe.getBehaviour(InvManipulationBehaviour.TYPE);
        Inventory handler = invManipulationBehaviour.getInventory();

        if (handler == null)
            return Stream.empty();

        ServerFilteringBehaviour filteringBehaviour = cobe.getBehaviour(ServerFilteringBehaviour.TYPE);
        return new CountedItemStackList(handler, filteringBehaviour).getTopNames(maxRows);
    }

    @Override
    protected String getTranslationKey() {
        return "list_items";
    }

    @Override
    protected boolean valueFirst() {
        return true;
    }

}

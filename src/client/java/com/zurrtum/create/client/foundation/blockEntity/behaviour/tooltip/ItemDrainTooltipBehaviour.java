package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.content.fluids.drain.ItemDrainBlockEntity;
import net.minecraft.text.Text;

import java.util.List;

public class ItemDrainTooltipBehaviour extends TooltipBehaviour<ItemDrainBlockEntity> implements IHaveGoggleInformation {
    public ItemDrainTooltipBehaviour(ItemDrainBlockEntity be) {
        super(be);
    }

    @Override
    public boolean addToGoggleTooltip(List<Text> tooltip, boolean isPlayerSneaking) {
        return containedFluidTooltip(tooltip, isPlayerSneaking, blockEntity.internalTank.getCapability());
    }
}

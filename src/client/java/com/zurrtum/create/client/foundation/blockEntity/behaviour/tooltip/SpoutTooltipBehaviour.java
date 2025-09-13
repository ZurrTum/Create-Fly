package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.content.fluids.spout.SpoutBlockEntity;
import net.minecraft.text.Text;

import java.util.List;

public class SpoutTooltipBehaviour extends TooltipBehaviour<SpoutBlockEntity> implements IHaveGoggleInformation {
    public SpoutTooltipBehaviour(SpoutBlockEntity be) {
        super(be);
    }

    @Override
    public boolean addToGoggleTooltip(List<Text> tooltip, boolean isPlayerSneaking) {
        return containedFluidTooltip(tooltip, isPlayerSneaking, blockEntity.tank.getCapability());
    }
}

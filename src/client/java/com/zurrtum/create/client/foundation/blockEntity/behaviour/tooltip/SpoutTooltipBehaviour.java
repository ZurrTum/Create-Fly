package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.content.fluids.spout.SpoutBlockEntity;
import java.util.List;
import net.minecraft.network.chat.Component;

public class SpoutTooltipBehaviour extends TooltipBehaviour<SpoutBlockEntity> implements IHaveGoggleInformation {
    public SpoutTooltipBehaviour(SpoutBlockEntity be) {
        super(be);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return containedFluidTooltip(tooltip, isPlayerSneaking, blockEntity.tank.getCapability());
    }
}

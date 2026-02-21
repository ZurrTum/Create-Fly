package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import net.minecraft.network.chat.Component;

import java.util.List;

public class PoweredShaftTooltipBehaviour extends GeneratingKineticTooltipBehaviour<PoweredShaftBlockEntity> implements IHaveGoggleInformation {
    public PoweredShaftTooltipBehaviour(PoweredShaftBlockEntity be) {
        super(be);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return false;
    }

    public boolean addToEngineTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return super.addToGoggleTooltip(tooltip, isPlayerSneaking);
    }
}

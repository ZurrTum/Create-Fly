package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import java.util.List;
import net.minecraft.network.chat.Component;

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

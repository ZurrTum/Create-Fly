package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import com.zurrtum.create.content.kinetics.steamEngine.SteamEngineBlockEntity;

import java.util.List;

import net.minecraft.network.chat.Component;

public class SteamEngineTooltipBehaviour extends TooltipBehaviour<SteamEngineBlockEntity> implements IHaveGoggleInformation {

    public SteamEngineTooltipBehaviour(SteamEngineBlockEntity be) {
        super(be);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        PoweredShaftBlockEntity shaft = blockEntity.getShaft();
        if (shaft == null) {
            return false;
        }
        PoweredShaftTooltipBehaviour behaviour = (PoweredShaftTooltipBehaviour) shaft.getBehaviour(TYPE);
        return behaviour != null && behaviour.addToEngineTooltip(tooltip, isPlayerSneaking);
    }
}

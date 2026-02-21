package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.redstone.analogLever.AnalogLeverBlockEntity;
import net.minecraft.network.chat.Component;

import java.util.List;

public class AnalogLeverTooltipBehaviour extends TooltipBehaviour<AnalogLeverBlockEntity> implements IHaveGoggleInformation {
    public AnalogLeverTooltipBehaviour(AnalogLeverBlockEntity be) {
        super(be);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("tooltip.analogStrength", blockEntity.getState()).forGoggles(tooltip);

        return true;
    }
}

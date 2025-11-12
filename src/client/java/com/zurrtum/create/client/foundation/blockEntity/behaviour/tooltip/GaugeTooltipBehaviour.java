package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.gauge.GaugeBlockEntity;

import java.util.List;

import net.minecraft.network.chat.Component;

public class GaugeTooltipBehaviour<T extends GaugeBlockEntity> extends KineticTooltipBehaviour<T> {
    public GaugeTooltipBehaviour(T be) {
        super(be);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("gui.gauge.info_header").forGoggles(tooltip);

        return true;
    }
}

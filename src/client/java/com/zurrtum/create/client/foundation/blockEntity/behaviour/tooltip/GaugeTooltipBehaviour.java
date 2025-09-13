package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.gauge.GaugeBlockEntity;
import net.minecraft.text.Text;

import java.util.List;

public class GaugeTooltipBehaviour<T extends GaugeBlockEntity> extends KineticTooltipBehaviour<T> {
    public GaugeTooltipBehaviour(T be) {
        super(be);
    }

    @Override
    public boolean addToGoggleTooltip(List<Text> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("gui.gauge.info_header").forGoggles(tooltip);

        return true;
    }
}

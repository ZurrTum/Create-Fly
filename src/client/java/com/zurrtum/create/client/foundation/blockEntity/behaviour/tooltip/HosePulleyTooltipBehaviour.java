package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.content.fluids.hosePulley.HosePulleyBlockEntity;
import net.minecraft.text.Text;

import java.util.List;

public class HosePulleyTooltipBehaviour extends KineticTooltipBehaviour<HosePulleyBlockEntity> {
    public HosePulleyTooltipBehaviour(HosePulleyBlockEntity be) {
        super(be);
    }

    @Override
    public boolean addToGoggleTooltip(List<Text> tooltip, boolean isPlayerSneaking) {
        boolean addToGoggleTooltip = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        if (blockEntity.infinite)
            TooltipHelper.addHint(tooltip, "hint.hose_pulley");
        return addToGoggleTooltip;
    }
}

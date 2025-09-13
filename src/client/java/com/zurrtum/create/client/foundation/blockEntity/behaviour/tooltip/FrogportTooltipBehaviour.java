package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.api.goggles.IHaveHoveringInformation;
import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import net.minecraft.text.Text;

import java.util.List;

public class FrogportTooltipBehaviour extends TooltipBehaviour<FrogportBlockEntity> implements IHaveHoveringInformation {
    public FrogportTooltipBehaviour(FrogportBlockEntity be) {
        super(be);
    }

    @Override
    public boolean addToTooltip(List<Text> tooltip, boolean isPlayerSneaking) {
        boolean superTip = IHaveHoveringInformation.super.addToTooltip(tooltip, isPlayerSneaking);
        if (!blockEntity.failedLastExport)
            return superTip;
        TooltipHelper.addHint(tooltip, "hint.blocked_frogport");
        return true;
    }
}

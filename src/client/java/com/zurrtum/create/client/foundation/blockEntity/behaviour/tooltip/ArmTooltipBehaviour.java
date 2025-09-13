package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import net.minecraft.text.Text;

import java.util.List;

public class ArmTooltipBehaviour extends KineticTooltipBehaviour<ArmBlockEntity> {
    public ArmTooltipBehaviour(ArmBlockEntity be) {
        super(be);
    }

    @Override
    public boolean addToTooltip(List<Text> tooltip, boolean isPlayerSneaking) {
        if (super.addToTooltip(tooltip, isPlayerSneaking))
            return true;
        if (isPlayerSneaking)
            return false;
        if (blockEntity.tooltipWarmup > 0)
            return false;
        if (!blockEntity.inputs.isEmpty())
            return false;
        if (!blockEntity.outputs.isEmpty())
            return false;

        TooltipHelper.addHint(tooltip, "hint.mechanical_arm_no_targets");
        return true;
    }
}

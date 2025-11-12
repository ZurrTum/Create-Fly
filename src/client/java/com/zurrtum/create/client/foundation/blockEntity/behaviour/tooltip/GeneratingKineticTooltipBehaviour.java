package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.base.IRotate.StressImpact;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import static net.minecraft.ChatFormatting.*;

public class GeneratingKineticTooltipBehaviour<T extends KineticBlockEntity> extends KineticTooltipBehaviour<T> {

    public GeneratingKineticTooltipBehaviour(T be) {
        super(be);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (!StressImpact.isEnabled())
            return false;
        boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        float stressBase = blockEntity.calculateAddedStressCapacity();
        if (Mth.equal(stressBase, 0))
            return added;

        CreateLang.translate("gui.goggles.generator_stats").forGoggles(tooltip);
        CreateLang.translate("tooltip.capacityProvided").style(GRAY).forGoggles(tooltip);

        float speed = blockEntity.getTheoreticalSpeed();
        if (speed != blockEntity.getGeneratedSpeed() && speed != 0)
            stressBase *= blockEntity.getGeneratedSpeed() / speed;

        float stressTotal = Math.abs(stressBase * speed);

        CreateLang.number(stressTotal).translate("generic.unit.stress").style(AQUA).space()
            .add(CreateLang.translate("gui.goggles.at_current_speed").style(DARK_GRAY)).forGoggles(tooltip, 1);

        return true;
    }
}

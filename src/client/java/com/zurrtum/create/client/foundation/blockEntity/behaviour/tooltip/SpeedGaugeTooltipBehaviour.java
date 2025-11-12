package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.catnip.lang.LangBuilder;
import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.base.IRotate.SpeedLevel;
import com.zurrtum.create.content.kinetics.gauge.SpeedGaugeBlockEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class SpeedGaugeTooltipBehaviour extends GaugeTooltipBehaviour<SpeedGaugeBlockEntity> {
    public SpeedGaugeTooltipBehaviour(SpeedGaugeBlockEntity be) {
        super(be);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        CreateLang.translate("gui.speedometer.title").style(ChatFormatting.GRAY).forGoggles(tooltip);
        getFormattedSpeedText(blockEntity.getSpeed(), blockEntity.isOverStressed()).forGoggles(tooltip);
        return true;
    }

    public static LangBuilder getFormattedSpeedText(float speed, boolean overstressed) {
        SpeedLevel speedLevel = SpeedLevel.of(speed);
        LangBuilder builder = CreateLang.text(TooltipHelper.makeProgressBar(3, speedLevel.ordinal()));

        builder.translate("tooltip.speedRequirement." + Lang.asId(speedLevel.name())).space().text("(").add(CreateLang.number(Math.abs(speed)))
            .space().translate("generic.unit.rpm").text(")").space();

        if (overstressed)
            builder.style(ChatFormatting.DARK_GRAY).style(ChatFormatting.STRIKETHROUGH);
        else
            builder.style(speedLevel.getTextColor());

        return builder;
    }
}

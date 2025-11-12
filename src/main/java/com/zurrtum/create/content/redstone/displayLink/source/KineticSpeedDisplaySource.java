package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.content.kinetics.gauge.SpeedGaugeBlockEntity;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;

import java.text.NumberFormat;
import java.util.Locale;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

public class KineticSpeedDisplaySource extends NumericSingleLineDisplaySource {
    private final NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (!(context.getSourceBlockEntity() instanceof SpeedGaugeBlockEntity speedGauge))
            return ZERO.copy();

        boolean absoluteValue = context.sourceConfig().getIntOr("Directional", 0) == 0;
        float speed = absoluteValue ? Math.abs(speedGauge.getSpeed()) : speedGauge.getSpeed();
        if (Mth.equal(speed, 0)) {
            speed = 0;
        }
        return Component.literal(format.format(speed).replace("\u00A0", " ")).append(Component.literal(" "))
            .append(Component.translatable("create.generic.unit.rpm"));
    }

    @Override
    protected String getTranslationKey() {
        return "kinetic_speed";
    }

    @Override
    public boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}
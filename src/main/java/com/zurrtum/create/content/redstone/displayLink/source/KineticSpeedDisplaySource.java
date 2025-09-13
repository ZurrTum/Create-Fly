package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.content.kinetics.gauge.SpeedGaugeBlockEntity;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.text.NumberFormat;
import java.util.Locale;

public class KineticSpeedDisplaySource extends NumericSingleLineDisplaySource {
    private final NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);

    @Override
    protected MutableText provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (!(context.getSourceBlockEntity() instanceof SpeedGaugeBlockEntity speedGauge))
            return ZERO.copy();

        boolean absoluteValue = context.sourceConfig().getInt("Directional", 0) == 0;
        float speed = absoluteValue ? Math.abs(speedGauge.getSpeed()) : speedGauge.getSpeed();
        if (MathHelper.approximatelyEquals(speed, 0)) {
            speed = 0;
        }
        return Text.literal(format.format(speed).replace("\u00A0", " ")).append(Text.literal(" "))
            .append(Text.translatable("create.generic.unit.rpm"));
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
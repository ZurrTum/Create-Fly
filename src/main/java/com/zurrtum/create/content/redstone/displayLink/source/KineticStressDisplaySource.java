package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.content.kinetics.gauge.StressGaugeBlockEntity;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.trains.display.FlapDisplayBlockEntity;
import java.text.NumberFormat;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

public class KineticStressDisplaySource extends PercentOrProgressBarDisplaySource {
    private final NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);

    @Override
    protected MutableComponent formatNumeric(DisplayLinkContext context, Float currentLevel) {
        int mode = getMode(context);
        if (mode == 1)
            return super.formatNumeric(context, currentLevel);
        if (Mth.equal(currentLevel, 0)) {
            currentLevel = 0f;
        }
        MutableComponent text = Component.literal(format.format(currentLevel).replace("\u00A0", " "));
        if (context.getTargetBlockEntity() instanceof FlapDisplayBlockEntity)
            text.append(Component.literal(" "));
        return text.append(Component.translatable("create.generic.unit.stress"));
    }

    private int getMode(DisplayLinkContext context) {
        return context.sourceConfig().getIntOr("Mode", 0);
    }

    @Override
    protected Float getProgress(DisplayLinkContext context) {
        if (!(context.getSourceBlockEntity() instanceof StressGaugeBlockEntity stressGauge))
            return null;

        float capacity = stressGauge.getNetworkCapacity();
        float stress = stressGauge.getNetworkStress();

        if (capacity == 0)
            return 0f;

        return switch (getMode(context)) {
            case 0, 1 -> stress / capacity;
            case 2 -> stress;
            case 3 -> capacity;
            case 4 -> capacity - stress;
            default -> 0f;
        };
    }

    @Override
    public boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }

    @Override
    protected boolean progressBarActive(DisplayLinkContext context) {
        return getMode(context) == 0;
    }

    @Override
    protected String getTranslationKey() {
        return "kinetic_stress";
    }

}
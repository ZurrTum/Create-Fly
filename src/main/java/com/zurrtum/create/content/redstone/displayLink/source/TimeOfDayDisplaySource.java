package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.content.kinetics.clock.CuckooClockBlockEntity;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.zurrtum.create.content.trains.display.FlapDisplaySection;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class TimeOfDayDisplaySource extends SingleLineDisplaySource {

    public static final MutableText EMPTY_TIME;

    static {
        EMPTY_TIME = Text.literal("--:--");
    }

    @Override
    protected MutableText provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (!(context.level() instanceof ServerWorld sLevel))
            return EMPTY_TIME;
        if (!(context.getSourceBlockEntity() instanceof CuckooClockBlockEntity ccbe))
            return EMPTY_TIME;
        if (ccbe.getSpeed() == 0)
            return EMPTY_TIME;

        boolean c12 = context.sourceConfig().getInt("Cycle", 0) == 0;
        boolean isNatural = sLevel.getDimension().natural();

        int dayTime = (int) (sLevel.getTimeOfDay() % 24000);
        int hours = (dayTime / 1000 + 6) % 24;
        int minutes = (dayTime % 1000) * 60 / 1000;
        MutableText suffix = Text.translatable("create.generic.daytime." + (hours > 11 ? "pm" : "am"));

        minutes = minutes / 5 * 5;
        if (c12) {
            hours %= 12;
            if (hours == 0)
                hours = 12;
        }

        if (!isNatural) {
            hours = sLevel.random.nextInt(70) + 24;
            minutes = sLevel.random.nextInt(40) + 60;
        }

        MutableText component = Text.literal((hours < 10 ? " " : "") + hours + ":" + (minutes < 10 ? "0" : "") + minutes + (c12 ? " " : ""));

        return c12 ? component.append(suffix) : component;
    }

    @Override
    protected String getFlapDisplayLayoutName(DisplayLinkContext context) {
        return "Instant";
    }

    @Override
    protected FlapDisplaySection createSectionForValue(DisplayLinkContext context, int size) {
        return new FlapDisplaySection(size * FlapDisplaySection.MONOSPACE, "instant", false, false);
    }

    @Override
    protected String getTranslationKey() {
        return "time_of_day";
    }

    @Override
    public boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }

}

package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.content.kinetics.clock.CuckooClockBlockEntity;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.zurrtum.create.content.trains.display.FlapDisplaySection;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class StopWatchDisplaySource extends SingleLineDisplaySource {

    @Override
    protected MutableText provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (!(context.getSourceBlockEntity() instanceof CuckooClockBlockEntity ccbe))
            return TimeOfDayDisplaySource.EMPTY_TIME;
        if (ccbe.getSpeed() == 0)
            return TimeOfDayDisplaySource.EMPTY_TIME;

        if (!context.sourceConfig().contains("StartTime"))
            onSignalReset(context);

        long started = context.sourceConfig().getLong("StartTime", 0);
        long current = context.blockEntity().getWorld().getTime();

        int diff = (int) (current - started);
        int hours = (diff / 60 / 60 / 20);
        int minutes = (diff / 60 / 20) % 60;
        int seconds = (diff / 20) % 60;

        MutableText component = Text.literal((hours == 0 ? "" : (hours < 10 ? " " : "") + hours + ":") + (minutes < 10 ? hours == 0 ? " " : "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds);

        return component;
    }

    @Override
    public void onSignalReset(DisplayLinkContext context) {
        context.sourceConfig().putLong("StartTime", context.blockEntity().getWorld().getTime());
    }

    @Override
    public int getPassiveRefreshTicks() {
        return 20;
    }

    @Override
    public boolean allowsLabeling(DisplayLinkContext context) {
        return true;
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
        return "stop_watch";
    }

}

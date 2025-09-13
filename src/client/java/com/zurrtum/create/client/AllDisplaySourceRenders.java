package com.zurrtum.create.client;

import com.zurrtum.create.AllDisplaySources;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.client.api.behaviour.display.DisplaySourceRender;
import com.zurrtum.create.client.content.redstone.displayLink.source.*;

import java.util.function.Supplier;

public class AllDisplaySourceRenders {
    public static void register(DisplaySource target, Supplier<DisplaySourceRender> factory) {
        target.attachRender = factory.get();
    }

    public static void register() {
        register(AllDisplaySources.ITEM_NAMES, SingleLineDisplaySourceRender::new);
        register(AllDisplaySources.TIME_OF_DAY, TimeOfDayDisplaySourceRender::new);
        register(AllDisplaySources.STOPWATCH, SingleLineDisplaySourceRender::new);
        register(AllDisplaySources.KINETIC_SPEED, KineticSpeedDisplaySourceRender::new);
        register(AllDisplaySources.KINETIC_STRESS, KineticStressDisplaySourceRender::new);
        register(AllDisplaySources.BOILER, SingleLineDisplaySourceRender::new);
        register(AllDisplaySources.CURRENT_FLOOR, SingleLineDisplaySourceRender::new);
        register(AllDisplaySources.STATION_SUMMARY, StationSummaryDisplaySourceRender::new);
        register(AllDisplaySources.TRAIN_STATUS, SingleLineDisplaySourceRender::new);
        register(AllDisplaySources.OBSERVED_TRAIN_NAME, SingleLineDisplaySourceRender::new);
        register(AllDisplaySources.ACCUMULATE_ITEMS, SingleLineDisplaySourceRender::new);
        register(AllDisplaySources.ITEM_THROUGHPUT, ItemThroughputDisplaySourceRender::new);
        register(AllDisplaySources.COUNT_ITEMS, SingleLineDisplaySourceRender::new);
        register(AllDisplaySources.LIST_ITEMS, ValueListDisplaySourceRender::new);
        register(AllDisplaySources.COUNT_FLUIDS, SingleLineDisplaySourceRender::new);
        register(AllDisplaySources.LIST_FLUIDS, ValueListDisplaySourceRender::new);
        register(AllDisplaySources.READ_PACKAGE_ADDRESS, SingleLineDisplaySourceRender::new);
        register(AllDisplaySources.FILL_LEVEL, FillLevelDisplaySourceRender::new);
        register(AllDisplaySources.GAUGE_STATUS, ValueListDisplaySourceRender::new);
        register(AllDisplaySources.ENTITY_NAME, SingleLineDisplaySourceRender::new);
        register(AllDisplaySources.DEATH_COUNT, StatTrackingDisplaySourceRender::new);
        register(AllDisplaySources.SCOREBOARD, ScoreboardDisplaySourceRender::new);
        register(AllDisplaySources.ENCHANT_POWER, SingleLineDisplaySourceRender::new);
        register(AllDisplaySources.REDSTONE_POWER, RedstonePowerDisplaySourceRender::new);
        register(AllDisplaySources.NIXIE_TUBE, SingleLineDisplaySourceRender::new);
    }
}

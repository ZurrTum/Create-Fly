package com.zurrtum.create.content.redstone.displayLink.source;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.zurrtum.create.content.trains.display.FlapDisplayBlockEntity;
import com.zurrtum.create.content.trains.display.FlapDisplayLayout;
import com.zurrtum.create.content.trains.display.FlapDisplaySection;
import com.zurrtum.create.content.trains.display.GlobalTrainDisplayData;
import com.zurrtum.create.content.trains.station.GlobalStation;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

import static com.zurrtum.create.content.trains.display.FlapDisplaySection.MONOSPACE;

public class StationSummaryDisplaySource extends DisplaySource {

    protected static final MutableText UNPREDICTABLE = Text.literal(" ~ ");
    protected static final List<MutableText> EMPTY_ENTRY_4 = ImmutableList.of(WHITESPACE, Text.literal(" . "), WHITESPACE, WHITESPACE);
    protected static final List<MutableText> EMPTY_ENTRY_5 = ImmutableList.of(WHITESPACE, Text.literal(" . "), WHITESPACE, WHITESPACE, WHITESPACE);

    @Override
    public List<MutableText> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
        return EMPTY;
    }

    @Override
    public List<List<MutableText>> provideFlapDisplayText(DisplayLinkContext context, DisplayTargetStats stats) {
        String filter = context.sourceConfig().getString("Filter", "");
        boolean hasPlatform = filter.contains("*");

        List<List<MutableText>> list = new ArrayList<>();
        GlobalTrainDisplayData.prepare(filter, stats.maxRows()).forEach(prediction -> {
            List<MutableText> lines = new ArrayList<>();

            if (prediction.ticks == -1 || prediction.ticks >= 12000 - 15 * 20) {
                lines.add(WHITESPACE);
                lines.add(UNPREDICTABLE);

            } else if (prediction.ticks < 200) {
                lines.add(WHITESPACE);
                lines.add(Text.translatable("create.display_source.station_summary.now"));

            } else {
                int min = prediction.ticks / 1200;
                int sec = (prediction.ticks / 20) % 60;
                sec = MathHelper.ceil(sec / 15f) * 15;
                if (sec == 60) {
                    min++;
                    sec = 0;
                }
                lines.add(min > 0 ? Text.literal(String.valueOf(min)) : WHITESPACE);
                lines.add(min > 0 ? Text.translatable("create.display_source.station_summary.minutes") : Text.translatable("create.display_source.station_summary.seconds",
                    sec
                ));
            }

            lines.add(prediction.train.name.copy());
            lines.add(prediction.scheduleTitle);

            if (!hasPlatform) {
                list.add(lines);
                return;
            }

            String platform = prediction.destination;
            for (String string : filter.split("\\*"))
                if (!string.isEmpty())
                    platform = platform.replace(string, "");
            platform = platform.replace("*", "?");

            lines.add(Text.literal(platform.trim()));
            list.add(lines);
        });

        if (!list.isEmpty())
            context.blockEntity().award(AllAdvancements.DISPLAY_BOARD);

        int toPad = stats.maxRows() - list.size();
        for (int padding = 0; padding < toPad; padding++)
            list.add(hasPlatform ? EMPTY_ENTRY_5 : EMPTY_ENTRY_4);

        return list;
    }

    @Override
    public void loadFlapDisplayLayout(DisplayLinkContext context, FlapDisplayBlockEntity flapDisplay, FlapDisplayLayout layout) {
        NbtCompound conf = context.sourceConfig();
        int columnWidth = conf.getInt("NameColumn", 0);
        int columnWidth2 = conf.getInt("PlatformColumn", 0);
        boolean hasPlatform = conf.getString("Filter", "").contains("*");

        String layoutName = "StationSummary" + columnWidth + hasPlatform + columnWidth2;

        if (layout.isLayout(layoutName))
            return;

        ArrayList<FlapDisplaySection> list = new ArrayList<>();

        int timeWidth = 20;
        float gapSize = 8f;
        float platformWidth = columnWidth2 * MONOSPACE;

        // populate
        FlapDisplaySection minutes = new FlapDisplaySection(MONOSPACE, "numeric", false, false);
        FlapDisplaySection time = new FlapDisplaySection(timeWidth, "arrival_time", true, true);

        float totalSize = flapDisplay.xSize * 32f - 4f - gapSize * 2;
        totalSize = totalSize - timeWidth - MONOSPACE;
        platformWidth = Math.min(platformWidth, totalSize - gapSize);
        platformWidth = (int) (platformWidth / MONOSPACE) * MONOSPACE;

        if (hasPlatform)
            totalSize = totalSize - gapSize - platformWidth;
        if (platformWidth == 0 && hasPlatform)
            totalSize += gapSize;

        int trainNameWidth = (int) ((columnWidth / 100f) * totalSize / MONOSPACE);
        int destinationWidth = (int) Math.round((1 - columnWidth / 100f) * totalSize / MONOSPACE);

        FlapDisplaySection trainName = new FlapDisplaySection(trainNameWidth * MONOSPACE, "alphabet", false, trainNameWidth > 0);
        FlapDisplaySection destination = new FlapDisplaySection(
            destinationWidth * MONOSPACE,
            "alphabet",
            false,
            hasPlatform && destinationWidth > 0 && platformWidth > 0
        );

        FlapDisplaySection platform = new FlapDisplaySection(platformWidth, "numeric", false, false).rightAligned();

        list.add(minutes);
        list.add(time);
        list.add(trainName);
        list.add(destination);

        if (hasPlatform)
            list.add(platform);

        layout.configure(layoutName, list);
    }

    @Override
    protected String getTranslationKey() {
        return "station_summary";
    }

    @Override
    public void populateData(DisplayLinkContext context) {
        NbtCompound conf = context.sourceConfig();

        if (!conf.contains("PlatformColumn"))
            conf.putInt("PlatformColumn", 3);
        if (!conf.contains("NameColumn"))
            conf.putInt("NameColumn", 50);

        if (conf.contains("Filter"))
            return;
        if (!(context.getSourceBlockEntity() instanceof StationBlockEntity stationBe))
            return;
        GlobalStation station = stationBe.getStation();
        if (station == null)
            return;
        conf.putString("Filter", station.name);
    }

}

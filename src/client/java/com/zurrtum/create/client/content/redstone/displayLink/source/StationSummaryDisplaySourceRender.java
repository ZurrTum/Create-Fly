package com.zurrtum.create.client.content.redstone.displayLink.source;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.client.api.behaviour.display.DisplaySourceRender;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import net.minecraft.util.Formatting;

public class StationSummaryDisplaySourceRender implements DisplaySourceRender {
    @Override
    public void initConfigurationWidgets(DisplaySource source, DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        if (isFirstLine) {
            builder.addTextInput(
                0, 137, (e, t) -> {
                    e.setText("");
                    t.withTooltip(ImmutableList.of(
                        CreateLang.translateDirect("display_source.station_summary.filter").withColor(0x5391E1),
                        CreateLang.translateDirect("gui.schedule.lmb_edit").formatted(Formatting.DARK_GRAY, Formatting.ITALIC)
                    ));
                }, "Filter"
            );
            return;
        }

        builder.addScrollInput(
            0, 32, (si, l) -> {
                si.titled(CreateLang.translateDirect("display_source.station_summary.train_name_column")).withRange(0, 73).withShiftStep(12);
                si.setState(50);
                l.withSuffix("%");
            }, "NameColumn"
        );

        builder.addScrollInput(
            36, 22, (si, l) -> {
                si.titled(CreateLang.translateDirect("display_source.station_summary.platform_column")).withRange(0, 16).withShiftStep(4);
                si.setState(3);
            }, "PlatformColumn"
        );
    }
}

package com.zurrtum.create.client.content.redstone.displayLink.source;

import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;

public class ItemThroughputDisplaySourceRender extends SingleLineDisplaySourceRender {
    @Override
    public void initConfigurationWidgets(DisplaySource source, DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        super.initConfigurationWidgets(source, context, builder, isFirstLine);
        if (isFirstLine)
            return;

        builder.addSelectionScrollInput(
            0, 80, (si, l) -> {
                si.forOptions(CreateLang.translatedOptions("display_source.item_throughput.interval", "second", "minute", "hour"))
                    .titled(CreateLang.translateDirect("display_source.item_throughput.interval"));
            }, "Interval"
        );
    }
}

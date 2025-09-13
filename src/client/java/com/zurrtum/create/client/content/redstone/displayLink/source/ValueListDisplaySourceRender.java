package com.zurrtum.create.client.content.redstone.displayLink.source;

import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.client.api.behaviour.display.DisplaySourceRender;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;

public class ValueListDisplaySourceRender implements DisplaySourceRender {
    @Override
    public void initConfigurationWidgets(DisplaySource source, DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        if (isFirstLine)
            addFullNumberConfig(builder);
    }

    protected void addFullNumberConfig(ModularGuiLineBuilder builder) {
        builder.addSelectionScrollInput(
            0,
            75,
            (si, l) -> si.forOptions(CreateLang.translatedOptions("display_source.value_list", "shortened", "full_number"))
                .titled(CreateLang.translateDirect("display_source.value_list.display")),
            "Format"
        );
    }
}

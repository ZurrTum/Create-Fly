package com.zurrtum.create.client.content.redstone.displayLink.source;

import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;

public class KineticSpeedDisplaySourceRender extends SingleLineDisplaySourceRender {
    @Override
    public void initConfigurationWidgets(DisplaySource source, DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        super.initConfigurationWidgets(source, context, builder, isFirstLine);
        if (isFirstLine)
            return;

        builder.addSelectionScrollInput(
            0, 95, (selectionScrollInput, label) -> {
                selectionScrollInput.forOptions(CreateLang.translatedOptions("display_source.kinetic_speed", "absolute", "directional"));
            }, "Directional"
        );
    }
}

package com.zurrtum.create.client.api.behaviour.display;

import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;

public interface DisplaySourceRender {
    void initConfigurationWidgets(DisplaySource source, DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine);
}

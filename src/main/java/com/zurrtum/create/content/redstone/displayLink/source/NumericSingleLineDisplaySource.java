package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.trains.display.FlapDisplaySection;
import net.minecraft.text.Text;

public abstract class NumericSingleLineDisplaySource extends SingleLineDisplaySource {
    protected static final Text ZERO = Text.literal("0");

    @Override
    protected String getFlapDisplayLayoutName(DisplayLinkContext context) {
        return "Number";
    }

    @Override
    protected FlapDisplaySection createSectionForValue(DisplayLinkContext context, int size) {
        return new FlapDisplaySection(size * FlapDisplaySection.MONOSPACE, "numeric", false, false);
    }
}

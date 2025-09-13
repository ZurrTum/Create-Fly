package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.zurrtum.create.content.redstone.displayLink.target.NixieTubeDisplayTarget;
import com.zurrtum.create.content.redstone.nixieTube.NixieTubeBlockEntity;
import com.zurrtum.create.content.trains.display.FlapDisplaySection;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.text.MutableText;

public class NixieTubeDisplaySource extends SingleLineDisplaySource {

    @Override
    protected String getTranslationKey() {
        return "nixie_tube";
    }

    @Override
    protected MutableText provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        BlockEntity sourceBE = context.getSourceBlockEntity();
        if (!(sourceBE instanceof NixieTubeBlockEntity nbe))
            return EMPTY_LINE;

        MutableText text = nbe.getFullText();

        try {
            String line = text.getString();
            Integer.valueOf(line);
            context.flapDisplayContext = Boolean.TRUE;
        } catch (NumberFormatException e) {
        }

        return text;
    }

    @Override
    public boolean allowsLabeling(DisplayLinkContext context) {
        return !(context.blockEntity().activeTarget instanceof NixieTubeDisplayTarget);
    }

    @Override
    protected String getFlapDisplayLayoutName(DisplayLinkContext context) {
        if (isNumeric(context))
            return "Number";
        return super.getFlapDisplayLayoutName(context);
    }

    @Override
    protected FlapDisplaySection createSectionForValue(DisplayLinkContext context, int size) {
        if (isNumeric(context))
            return new FlapDisplaySection(size * FlapDisplaySection.MONOSPACE, "numeric", false, false);
        return super.createSectionForValue(context, size);
    }

    protected boolean isNumeric(DisplayLinkContext context) {
        return context.flapDisplayContext == Boolean.TRUE;
    }

}

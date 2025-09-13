package com.zurrtum.create.content.redstone.displayLink.source;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.zurrtum.create.content.trains.display.FlapDisplayBlockEntity;
import com.zurrtum.create.content.trains.display.FlapDisplayLayout;
import com.zurrtum.create.content.trains.display.FlapDisplaySection;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.List;

public abstract class SingleLineDisplaySource extends DisplaySource {

    protected abstract MutableText provideLine(DisplayLinkContext context, DisplayTargetStats stats);

    public abstract boolean allowsLabeling(DisplayLinkContext context);

    @Override
    public List<MutableText> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
        MutableText line = provideLine(context, stats);
        if (line == EMPTY_LINE)
            return EMPTY;

        if (allowsLabeling(context)) {
            String label = context.sourceConfig().getString("Label", "");
            if (!label.isEmpty()) {
                line = Text.literal(label + " ").append(line);
            }
        }

        return ImmutableList.of(line);
    }

    @Override
    public List<List<MutableText>> provideFlapDisplayText(DisplayLinkContext context, DisplayTargetStats stats) {

        if (allowsLabeling(context)) {
            String label = context.sourceConfig().getString("Label", "");
            if (!label.isEmpty()) {
                return ImmutableList.of(ImmutableList.of(Text.literal(label + " "), provideLine(context, stats)));
            }
        }

        return super.provideFlapDisplayText(context, stats);
    }

    @Override
    public void loadFlapDisplayLayout(DisplayLinkContext context, FlapDisplayBlockEntity flapDisplay, FlapDisplayLayout layout) {
        String layoutKey = getFlapDisplayLayoutName(context);

        if (!allowsLabeling(context)) {
            if (!layout.isLayout(layoutKey))
                layout.configure(layoutKey, ImmutableList.of(createSectionForValue(context, flapDisplay.getMaxCharCount())));
            return;
        }

        String label = context.sourceConfig().getString("Label", "");

        if (label.isEmpty()) {
            if (!layout.isLayout(layoutKey))
                layout.configure(layoutKey, ImmutableList.of(createSectionForValue(context, flapDisplay.getMaxCharCount())));
            return;
        }

        String layoutName = label.length() + "_Labeled_" + layoutKey;
        if (layout.isLayout(layoutName))
            return;

        int maxCharCount = flapDisplay.getMaxCharCount();
        FlapDisplaySection labelSection = new FlapDisplaySection(
            Math.min(maxCharCount, label.length() + 1) * FlapDisplaySection.MONOSPACE,
            "alphabet",
            false,
            false
        );

        if (label.length() + 1 < maxCharCount)
            layout.configure(layoutName, ImmutableList.of(labelSection, createSectionForValue(context, maxCharCount - label.length() - 1)));
        else
            layout.configure(layoutName, ImmutableList.of(labelSection));
    }

    protected String getFlapDisplayLayoutName(DisplayLinkContext context) {
        return "Default";
    }

    protected FlapDisplaySection createSectionForValue(DisplayLinkContext context, int size) {
        return new FlapDisplaySection(size * FlapDisplaySection.MONOSPACE, "alphabet", false, false);
    }

}

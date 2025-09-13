package com.zurrtum.create.content.redstone.displayLink.target;

import com.zurrtum.create.api.behaviour.display.DisplayTarget;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.List;

public abstract class SingleLineDisplayTarget extends DisplayTarget {

    @Override
    public final void acceptText(int line, List<MutableText> text, DisplayLinkContext context) {
        acceptLine(text.getFirst(), context);
    }

    protected abstract void acceptLine(MutableText text, DisplayLinkContext context);

    @Override
    public final DisplayTargetStats provideStats(DisplayLinkContext context) {
        return new DisplayTargetStats(1, getWidth(context), this);
    }

    @Override
    public Text getLineOptionText(int line) {
        return Text.translatable("create.display_target.single_line");
    }

    protected abstract int getWidth(DisplayLinkContext context);

}

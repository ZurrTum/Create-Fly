package com.zurrtum.create.content.redstone.displayLink.target;

import com.zurrtum.create.api.behaviour.display.DisplayTarget;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public abstract class SingleLineDisplayTarget extends DisplayTarget {

    @Override
    public final void acceptText(int line, List<MutableComponent> text, DisplayLinkContext context) {
        acceptLine(text.getFirst(), context);
    }

    protected abstract void acceptLine(MutableComponent text, DisplayLinkContext context);

    @Override
    public final DisplayTargetStats provideStats(DisplayLinkContext context) {
        return new DisplayTargetStats(1, getWidth(context), this);
    }

    @Override
    public Component getLineOptionText(int line) {
        return Component.translatable("create.display_target.single_line");
    }

    protected abstract int getWidth(DisplayLinkContext context);

}

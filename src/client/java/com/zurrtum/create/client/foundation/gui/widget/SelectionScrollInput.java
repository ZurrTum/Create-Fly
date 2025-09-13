package com.zurrtum.create.client.foundation.gui.widget;

import com.zurrtum.create.client.foundation.utility.CreateLang;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class SelectionScrollInput extends ScrollInput {

    private final MutableText scrollToSelect = CreateLang.translateDirect("gui.scrollInput.scrollToSelect");
    protected List<? extends Text> options;

    public SelectionScrollInput(int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn);
        options = new ArrayList<>();
        inverted();
    }

    public ScrollInput forOptions(List<? extends Text> options) {
        this.options = options;
        this.max = options.size();
        format(options::get);
        updateTooltip();
        return this;
    }

    @Override
    protected void updateTooltip() {
        toolTip.clear();
        if (title == null)
            return;
        toolTip.add(title.copyContentOnly().styled(s -> s.withColor(HEADER_RGB.getRGB())));
        int min = Math.min(this.max - 16, state - 7);
        int max = Math.max(this.min + 16, state + 8);
        min = Math.max(min, this.min);
        max = Math.min(max, this.max);
        if (this.min + 1 == min)
            min--;
        if (min > this.min) {
            toolTip.add(Text.literal("> ...").formatted(Formatting.GRAY));
        }
        if (this.max - 1 == max)
            max++;
        for (int i = min; i < max; i++) {
            if (i == state)
                toolTip.add(Text.empty().append("-> ").append(options.get(i)).formatted(Formatting.WHITE));
            else
                toolTip.add(Text.empty().append("> ").append(options.get(i)).formatted(Formatting.GRAY));
        }
        if (max < this.max) {
            toolTip.add(Text.literal("> ...").formatted(Formatting.GRAY));
        }

        if (hint != null)
            toolTip.add(hint.copyContentOnly().styled(s -> s.withColor(HINT_RGB.getRGB())));
        toolTip.add(scrollToSelect.copyContentOnly().formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
    }

}

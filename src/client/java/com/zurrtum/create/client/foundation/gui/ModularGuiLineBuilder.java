package com.zurrtum.create.client.foundation.gui;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.foundation.gui.widget.Label;
import com.zurrtum.create.client.foundation.gui.widget.ScrollInput;
import com.zurrtum.create.client.foundation.gui.widget.SelectionScrollInput;
import com.zurrtum.create.client.foundation.gui.widget.TooltipArea;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;

import java.util.function.BiConsumer;

public class ModularGuiLineBuilder {

    private final ModularGuiLine target;
    private final TextRenderer font;
    private final int x;
    private final int y;

    public ModularGuiLineBuilder(TextRenderer font, ModularGuiLine target, int x, int y) {
        this.font = font;
        this.target = target;
        this.x = x;
        this.y = y;
    }

    public ModularGuiLineBuilder addScrollInput(int x, int width, BiConsumer<ScrollInput, Label> inputTransform, String dataKey) {
        ScrollInput input = new ScrollInput(x + this.x, y - 4, width, 18);
        addScrollInput(input, inputTransform, dataKey);
        return this;
    }

    public ModularGuiLineBuilder addSelectionScrollInput(int x, int width, BiConsumer<SelectionScrollInput, Label> inputTransform, String dataKey) {
        SelectionScrollInput input = new SelectionScrollInput(x + this.x, y - 4, width, 18);
        addScrollInput(input, inputTransform, dataKey);
        return this;
    }

    public ModularGuiLineBuilder customArea(int x, int width) {
        target.customBoxes.add(Couple.create(x, width));
        return this;
    }

    public ModularGuiLineBuilder speechBubble() {
        target.speechBubble = true;
        return this;
    }

    private <T extends ScrollInput> void addScrollInput(T input, BiConsumer<T, Label> inputTransform, String dataKey) {
        Label label = new Label(input.getX() + 5, y, ScreenTexts.EMPTY);
        label.withShadow();
        inputTransform.accept(input, label);
        input.writingTo(label);
        target.add(Pair.of(label, "Dummy"));
        target.add(Pair.of(input, dataKey));
    }

    public ModularGuiLineBuilder addIntegerTextInput(int x, int width, BiConsumer<TextFieldWidget, TooltipArea> inputTransform, String dataKey) {
        return addTextInput(
            x, width, inputTransform.andThen((editBox, $) -> editBox.setTextPredicate(s -> {
                if (s.isEmpty())
                    return true;
                try {
                    Integer.parseInt(s);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            })), dataKey
        );
    }

    public ModularGuiLineBuilder addTextInput(int x, int width, BiConsumer<TextFieldWidget, TooltipArea> inputTransform, String dataKey) {
        TextFieldWidget input = new TextFieldWidget(font, x + this.x + 5, y, width - 9, 8, ScreenTexts.EMPTY);
        input.setDrawsBackground(false);
        input.setEditableColor(0xffffffff);
        input.setFocused(false);
        input.mouseClicked(0, 0, 0);
        TooltipArea tooltipArea = new TooltipArea(this.x + x, y - 4, width, 18);
        inputTransform.accept(input, tooltipArea);
        target.add(Pair.of(input, dataKey));
        target.add(Pair.of(tooltipArea, "Dummy"));
        return this;
    }

}

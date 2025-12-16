package com.zurrtum.create.client.foundation.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;

import java.util.Objects;
import java.util.function.Predicate;

public class FilterEditBox extends EditBox {
    private Predicate<String> filter;

    public FilterEditBox(Font font, int x, int y, int width, int height, Component narration) {
        super(font, x, y, width, height, null, narration);
        filter = Objects::nonNull;
    }

    public FilterEditBox(Font font, int x, int y, int width, int height, Component narration, Predicate<String> filter) {
        super(font, x, y, width, height, null, narration);
        this.filter = filter;
    }

    public void setFilter(final Predicate<String> filter) {
        this.filter = filter;
    }

    @Override
    public void setValue(String value) {
        if (filter.test(value)) {
            super.setValue(value);
        }
    }

    @Override
    public void insertText(String input) {
        int cursorPos = getCursorPosition();
        int start = Math.min(cursorPos, highlightPos);
        int end = Math.max(cursorPos, highlightPos);
        int maxInsertionLength = getMaxLength() - getValue().length() - (start - end);
        if (maxInsertionLength > 0) {
            String text = StringUtil.filterText(input);
            int insertionLength = text.length();
            if (maxInsertionLength < insertionLength) {
                if (Character.isHighSurrogate(text.charAt(maxInsertionLength - 1))) {
                    --maxInsertionLength;
                }

                text = text.substring(0, maxInsertionLength);
                insertionLength = maxInsertionLength;
            }

            String newValue = new StringBuilder(value).replace(start, end, text).toString();
            if (filter.test(newValue)) {
                value = newValue;
                setCursorPosition(start + insertionLength);
                setHighlightPos(getCursorPosition());
                onValueChange(value);
            }
        }
    }

    @Override
    public void deleteCharsToPos(int pos) {
        if (!value.isEmpty()) {
            int cursorPos = getCursorPosition();
            if (highlightPos != cursorPos) {
                insertText("");
            } else {
                int start = Math.min(pos, cursorPos);
                int end = Math.max(pos, cursorPos);
                if (start != end) {
                    String newValue = new StringBuilder(this.value).delete(start, end).toString();
                    if (filter.test(newValue)) {
                        value = newValue;
                        setCursorPosition(start);
                        onValueChange(this.value);
                        moveCursorTo(start, false);
                    }
                }
            }
        }
    }
}

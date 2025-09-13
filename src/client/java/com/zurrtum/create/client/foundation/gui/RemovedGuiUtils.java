package com.zurrtum.create.client.foundation.gui;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Language;

import java.util.ArrayList;
import java.util.List;

public class RemovedGuiUtils {
    public static final int DEFAULT_BACKGROUND_COLOR = 0xF0100010;
    public static final int DEFAULT_BORDER_COLOR_START = 0x505000FF;
    public static final int DEFAULT_BORDER_COLOR_END = (DEFAULT_BORDER_COLOR_START & 0xFEFEFE) >> 1 | DEFAULT_BORDER_COLOR_START & 0xFF000000;
    private static ItemStack cachedTooltipStack = ItemStack.EMPTY;

    public static void preItemToolTip(ItemStack stack) {
        cachedTooltipStack = stack;
    }

    public static void postItemToolTip() {
        cachedTooltipStack = ItemStack.EMPTY;
    }

    public static void drawHoveringText(
        DrawContext graphics,
        List<? extends StringVisitable> textLines,
        int mouseX,
        int mouseY,
        int screenWidth,
        int screenHeight,
        int maxTextWidth,
        TextRenderer font
    ) {
        drawHoveringText(
            graphics,
            textLines,
            mouseX,
            mouseY,
            screenWidth,
            screenHeight,
            maxTextWidth,
            DEFAULT_BACKGROUND_COLOR,
            DEFAULT_BORDER_COLOR_START,
            DEFAULT_BORDER_COLOR_END,
            font
        );
    }

    public static void drawHoveringText(
        DrawContext graphics,
        List<? extends StringVisitable> textLines,
        int mouseX,
        int mouseY,
        int screenWidth,
        int screenHeight,
        int maxTextWidth,
        int backgroundColor,
        int borderColorStart,
        int borderColorEnd,
        TextRenderer font
    ) {
        drawHoveringText(
            cachedTooltipStack,
            graphics,
            textLines,
            mouseX,
            mouseY,
            screenWidth,
            screenHeight,
            maxTextWidth,
            backgroundColor,
            borderColorStart,
            borderColorEnd,
            font
        );
    }

    public static void drawHoveringText(
        final ItemStack stack,
        DrawContext graphics,
        List<? extends StringVisitable> textLines,
        int mouseX,
        int mouseY,
        int screenWidth,
        int screenHeight,
        int maxTextWidth,
        TextRenderer font
    ) {
        drawHoveringText(
            stack,
            graphics,
            textLines,
            mouseX,
            mouseY,
            screenWidth,
            screenHeight,
            maxTextWidth,
            DEFAULT_BACKGROUND_COLOR,
            DEFAULT_BORDER_COLOR_START,
            DEFAULT_BORDER_COLOR_END,
            font
        );
    }

    public static void drawHoveringText(
        final ItemStack stack,
        DrawContext graphics,
        List<? extends StringVisitable> textLines,
        int mouseX,
        int mouseY,
        int screenWidth,
        int screenHeight,
        int maxTextWidth,
        int backgroundColor,
        int borderColorStart,
        int borderColorEnd,
        TextRenderer font
    ) {
        if (textLines.isEmpty())
            return;

        List<TooltipComponent> list = new ArrayList<>();
        for (StringVisitable textLine : textLines) {
            OrderedText charSequence = textLine instanceof Text text ? text.asOrderedText() : Language.getInstance().reorder(textLine);
            list.add(TooltipComponent.of(charSequence));
        }

        GlStateManager._disableDepthTest();
        int tooltipTextWidth = 0;

        for (StringVisitable textLine : textLines) {
            int textLineWidth = font.getWidth(textLine);
            if (textLineWidth > tooltipTextWidth)
                tooltipTextWidth = textLineWidth;
        }

        boolean needsWrap = false;

        int titleLinesCount = 1;
        int tooltipX = mouseX + 12;
        if (tooltipX + tooltipTextWidth + 4 > screenWidth) {
            tooltipX = mouseX - 16 - tooltipTextWidth;
            if (tooltipX < 4) // if the tooltip doesn't fit on the screen
            {
                if (mouseX > screenWidth / 2)
                    tooltipTextWidth = mouseX - 12 - 8;
                else
                    tooltipTextWidth = screenWidth - 16 - mouseX;
                needsWrap = true;
            }
        }

        if (maxTextWidth > 0 && tooltipTextWidth > maxTextWidth) {
            tooltipTextWidth = maxTextWidth;
            needsWrap = true;
        }

        if (needsWrap) {
            int wrappedTooltipWidth = 0;
            List<StringVisitable> wrappedTextLines = new ArrayList<>();
            for (int i = 0; i < textLines.size(); i++) {
                StringVisitable textLine = textLines.get(i);
                List<StringVisitable> wrappedLine = font.getTextHandler().wrapLines(textLine, tooltipTextWidth, Style.EMPTY);
                if (i == 0)
                    titleLinesCount = wrappedLine.size();

                for (StringVisitable line : wrappedLine) {
                    int lineWidth = font.getWidth(line);
                    if (lineWidth > wrappedTooltipWidth)
                        wrappedTooltipWidth = lineWidth;
                    wrappedTextLines.add(line);
                }
            }
            tooltipTextWidth = wrappedTooltipWidth;
            textLines = wrappedTextLines;

            if (mouseX > screenWidth / 2)
                tooltipX = mouseX - 16 - tooltipTextWidth;
            else
                tooltipX = mouseX + 12;
        }

        int tooltipY = mouseY - 12;
        int tooltipHeight = 8;

        if (textLines.size() > 1) {
            tooltipHeight += (textLines.size() - 1) * 10;
            if (textLines.size() > titleLinesCount)
                tooltipHeight += 2; // gap between title lines and next lines
        }

        if (tooltipY < 4)
            tooltipY = 4;
        else if (tooltipY + tooltipHeight + 4 > screenHeight)
            tooltipY = screenHeight - tooltipHeight - 4;

        graphics.fillGradient(tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3, tooltipY - 3, backgroundColor, backgroundColor);
        graphics.fillGradient(
            tooltipX - 3,
            tooltipY + tooltipHeight + 3,
            tooltipX + tooltipTextWidth + 3,
            tooltipY + tooltipHeight + 4,
            backgroundColor,
            backgroundColor
        );
        graphics.fillGradient(
            tooltipX - 3,
            tooltipY - 3,
            tooltipX + tooltipTextWidth + 3,
            tooltipY + tooltipHeight + 3,
            backgroundColor,
            backgroundColor
        );
        graphics.fillGradient(tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
        graphics.fillGradient(
            tooltipX + tooltipTextWidth + 3,
            tooltipY - 3,
            tooltipX + tooltipTextWidth + 4,
            tooltipY + tooltipHeight + 3,
            backgroundColor,
            backgroundColor
        );
        graphics.fillGradient(tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
        graphics.fillGradient(
            tooltipX + tooltipTextWidth + 2,
            tooltipY - 3 + 1,
            tooltipX + tooltipTextWidth + 3,
            tooltipY + tooltipHeight + 3 - 1,
            borderColorStart,
            borderColorEnd
        );
        graphics.fillGradient(tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY - 3 + 1, borderColorStart, borderColorStart);
        graphics.fillGradient(
            tooltipX - 3,
            tooltipY + tooltipHeight + 2,
            tooltipX + tooltipTextWidth + 3,
            tooltipY + tooltipHeight + 3,
            borderColorEnd,
            borderColorEnd
        );

        for (int lineNumber = 0; lineNumber < list.size(); ++lineNumber) {
            TooltipComponent line = list.get(lineNumber);

            if (line != null)
                line.drawText(graphics, font, tooltipX, tooltipY);

            if (lineNumber + 1 == titleLinesCount)
                tooltipY += 2;

            tooltipY += line == null ? 10 : line.getHeight(font);
        }

        GlStateManager._enableDepthTest();
    }
}

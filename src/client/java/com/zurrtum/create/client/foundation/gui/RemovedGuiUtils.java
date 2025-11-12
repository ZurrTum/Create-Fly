package com.zurrtum.create.client.foundation.gui;

import com.mojang.blaze3d.opengl.GlStateManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

public class RemovedGuiUtils {
    public static final int DEFAULT_BACKGROUND_COLOR = 0xF0100010;
    public static final int DEFAULT_BORDER_COLOR_START = 0x505000FF;
    public static final int DEFAULT_BORDER_COLOR_END = (DEFAULT_BORDER_COLOR_START & 0xFEFEFE) >> 1 | DEFAULT_BORDER_COLOR_START & 0xFF000000;
    @NotNull
    private static ItemStack cachedTooltipStack = ItemStack.EMPTY;

    public static void preItemToolTip(@NotNull ItemStack stack) {
        cachedTooltipStack = stack;
    }

    public static void postItemToolTip() {
        cachedTooltipStack = ItemStack.EMPTY;
    }

    public static void drawHoveringText(
        GuiGraphics graphics,
        List<? extends FormattedText> textLines,
        int mouseX,
        int mouseY,
        int screenWidth,
        int screenHeight,
        int maxTextWidth,
        Font font
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
        GuiGraphics graphics,
        List<? extends FormattedText> textLines,
        int mouseX,
        int mouseY,
        int screenWidth,
        int screenHeight,
        int maxTextWidth,
        int backgroundColor,
        int borderColorStart,
        int borderColorEnd,
        Font font
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
        @NotNull final ItemStack stack,
        GuiGraphics graphics,
        List<? extends FormattedText> textLines,
        int mouseX,
        int mouseY,
        int screenWidth,
        int screenHeight,
        int maxTextWidth,
        Font font
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
        @NotNull final ItemStack stack,
        GuiGraphics graphics,
        List<? extends FormattedText> textLines,
        int mouseX,
        int mouseY,
        int screenWidth,
        int screenHeight,
        int maxTextWidth,
        int backgroundColor,
        int borderColorStart,
        int borderColorEnd,
        Font font
    ) {
        if (textLines.isEmpty())
            return;

        List<ClientTooltipComponent> list = new ArrayList<>();
        for (FormattedText textLine : textLines) {
            FormattedCharSequence charSequence = textLine instanceof Component text ? text.getVisualOrderText() : Language.getInstance()
                .getVisualOrder(textLine);
            list.add(ClientTooltipComponent.create(charSequence));
        }

        GlStateManager._disableDepthTest();
        int tooltipTextWidth = 0;

        for (FormattedText textLine : textLines) {
            int textLineWidth = font.width(textLine);
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
            List<FormattedText> wrappedTextLines = new ArrayList<>();
            for (int i = 0; i < textLines.size(); i++) {
                FormattedText textLine = textLines.get(i);
                List<FormattedText> wrappedLine = font.getSplitter().splitLines(textLine, tooltipTextWidth, Style.EMPTY);
                if (i == 0)
                    titleLinesCount = wrappedLine.size();

                for (FormattedText line : wrappedLine) {
                    int lineWidth = font.width(line);
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
            ClientTooltipComponent line = list.get(lineNumber);

            if (line != null)
                line.renderText(graphics, font, tooltipX, tooltipY);

            if (lineNumber + 1 == titleLinesCount)
                tooltipY += 2;

            tooltipY += line == null ? 10 : line.getHeight(font);
        }

        GlStateManager._enableDepthTest();
    }
}

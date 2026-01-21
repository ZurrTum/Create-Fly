package com.zurrtum.create.client.catnip.lang;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.LightCoordsUtil;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

import java.text.BreakIterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class ClientFontHelper {

    public static List<String> cutString(Font font, String text, int maxWidthPerLine) {
        // Split words
        List<String> words = new LinkedList<>();
        String selected = Minecraft.getInstance().getLanguageManager().getSelected();
        final String[] langSplit = selected.split("_", 2);
        Locale locale = langSplit.length == 1 ? Locale.of(langSplit[0]) : Locale.of(langSplit[0], langSplit[1]);
        BreakIterator iterator = BreakIterator.getLineInstance(locale);
        iterator.setText(text);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String word = text.substring(start, end);
            words.add(word);
        }
        // Apply hard wrap
        List<String> lines = new LinkedList<>();
        StringBuilder currentLine = new StringBuilder();
        int width = 0;
        for (String word : words) {
            int newWidth = font.width(word);
            if (width + newWidth > maxWidthPerLine) {
                if (width > 0) {
                    String line = currentLine.toString();
                    lines.add(line);
                    currentLine = new StringBuilder();
                    width = 0;
                } else {
                    lines.add(word);
                    continue;
                }
            }
            currentLine.append(word);
            width += newWidth;
        }
        if (width > 0) {
            lines.add(currentLine.toString());
        }
        return lines;
    }

    public static void drawSplitString(GuiGraphics graphics, Font font, String text, int x, int y, int width, int color) {
        List<String> list = cutString(font, text, width);

        boolean rightToLeft = font.isBidirectional();
        for (String s : list) {
            int f = x;
            if (rightToLeft) {
                int i = font.width(font.bidirectionalShaping(s));
                f += width - i;
            }

            draw(graphics, font, s, f, y, color);
            y += 9;
        }
    }

    private static void draw(GuiGraphics graphics, Font font, @Nullable String text, int x, int y, int color) {
        if (text != null) {
            graphics.drawString(font, text, x, y, color, false);
        }
    }

    public static void drawSplitString(MultiBufferSource buffer, PoseStack matrixStack, Font font, String text, int x, int y, int width, int color) {
        List<String> list = cutString(font, text, width);
        Matrix4f matrix4f = matrixStack.last().pose();

        boolean rightToLeft = font.isBidirectional();
        for (String s : list) {
            int f = x;
            if (rightToLeft) {
                int i = font.width(font.bidirectionalShaping(s));
                f += width - i;
            }

            draw(buffer, font, s, f, y, color, matrix4f);
            y += 9;
        }
    }

    private static void draw(MultiBufferSource buffer, Font font, String text, int x, int y, int color, Matrix4f matrix4f) {
        font.drawInBatch(text, x, y, color, false, matrix4f, buffer, Font.DisplayMode.NORMAL, 0, LightCoordsUtil.FULL_BRIGHT);
    }
}
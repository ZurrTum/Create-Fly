package com.zurrtum.create.client.catnip.lang;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

import java.text.BreakIterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class ClientFontHelper {

    public static List<String> cutString(TextRenderer font, String text, int maxWidthPerLine) {
        // Split words
        List<String> words = new LinkedList<>();
        String selected = MinecraftClient.getInstance().getLanguageManager().getLanguage();
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
            int newWidth = font.getWidth(word);
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

    public static void drawSplitString(DrawContext graphics, TextRenderer font, String text, int x, int y, int width, int color) {
        List<String> list = cutString(font, text, width);

        boolean rightToLeft = font.isRightToLeft();
        for (String s : list) {
            int f = x;
            if (rightToLeft) {
                int i = font.getWidth(font.mirror(s));
                f += width - i;
            }

            draw(graphics, font, s, f, y, color);
            y += 9;
        }
    }

    private static void draw(DrawContext graphics, TextRenderer font, String text, int x, int y, int color) {
        if (text != null) {
            graphics.drawText(font, text, x, y, color, false);
        }
    }

    public static void drawSplitString(
        VertexConsumerProvider buffer,
        MatrixStack matrixStack,
        TextRenderer font,
        String text,
        int x,
        int y,
        int width,
        int color
    ) {
        List<String> list = cutString(font, text, width);
        Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();

        boolean rightToLeft = font.isRightToLeft();
        for (String s : list) {
            int f = x;
            if (rightToLeft) {
                int i = font.getWidth(font.mirror(s));
                f += width - i;
            }

            draw(buffer, font, s, f, y, color, matrix4f);
            y += 9;
        }
    }

    private static void draw(VertexConsumerProvider buffer, TextRenderer font, String text, int x, int y, int color, Matrix4f matrix4f) {
        font.draw(text, x, y, color, false, matrix4f, buffer, TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
    }
}
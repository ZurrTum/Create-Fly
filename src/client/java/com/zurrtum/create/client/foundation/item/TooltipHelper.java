package com.zurrtum.create.client.foundation.item;

import com.google.common.base.Strings;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.client.catnip.lang.FontHelper;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class TooltipHelper {

    public static final int MAX_WIDTH_PER_LINE = 200;

    public static MutableText holdShift(FontHelper.Palette palette, boolean highlighted) {
        return CreateLang.translateDirect("tooltip.holdForDescription", CreateLang.translateDirect("tooltip.keyShift").formatted(Formatting.GRAY))
            .formatted(Formatting.DARK_GRAY);
    }

    public static String makeProgressBar(int length, int filledLength) {
        String bar = " ";
        int emptySpaces = length - filledLength;
        for (int i = 0; i < filledLength; i++)
            bar += "\u2588";
        for (int i = 0; i < emptySpaces; i++)
            bar += "\u2592";
        return bar + " ";
    }

    public static Style styleFromColor(Formatting color) {
        return Style.EMPTY.withFormatting(color);
    }

    public static Style styleFromColor(int hex) {
        return Style.EMPTY.withColor(hex);
    }

    public static void addHint(List<Text> tooltip, String hintKey, Object... messageParams) {
        CreateLang.translate(hintKey + ".title").style(Formatting.GOLD).forGoggles(tooltip);
        Text hint = CreateLang.translateDirect(hintKey);
        List<Text> cutComponent = cutTextComponent(hint, FontHelper.Palette.GRAY_AND_WHITE);
        for (Text component : cutComponent)
            CreateLang.builder().add(component).forGoggles(tooltip);
    }

    public static List<Text> cutStringTextComponent(String s, FontHelper.Palette palette) {
        return cutTextComponent(Text.literal(s), palette);
    }

    public static List<Text> cutTextComponent(Text c, FontHelper.Palette palette) {
        return cutTextComponent(c, palette.primary(), palette.highlight());
    }

    public static List<Text> cutStringTextComponent(String s, Style primaryStyle, Style highlightStyle) {
        return cutTextComponent(Text.literal(s), primaryStyle, highlightStyle);
    }

    public static List<Text> cutTextComponent(Text c, Style primaryStyle, Style highlightStyle) {
        return cutTextComponent(c, primaryStyle, highlightStyle, 0);
    }

    public static List<Text> cutStringTextComponent(String c, Style primaryStyle, Style highlightStyle, int indent) {
        return cutTextComponent(Text.literal(c), primaryStyle, highlightStyle, indent);
    }

    public static List<Text> cutTextComponent(Text c, Style primaryStyle, Style highlightStyle, int indent) {
        String s = c.getString();

        // Split words
        List<String> words = new LinkedList<>();
        String selected = MinecraftClient.getInstance().getLanguageManager().getLanguage();
        String[] langSplit = selected.split("_", 2);
        Locale javaLocale = langSplit.length == 1 ? Locale.of(langSplit[0]) : Locale.of(langSplit[0], langSplit[1]);
        BreakIterator iterator = BreakIterator.getLineInstance(javaLocale);
        iterator.setText(s);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String word = s.substring(start, end);
            words.add(word);
        }

        // Apply hard wrap
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        List<String> lines = new LinkedList<>();
        StringBuilder currentLine = new StringBuilder();
        int width = 0;
        for (String word : words) {
            int newWidth = font.getWidth(word.replaceAll("_", ""));
            if (width + newWidth > MAX_WIDTH_PER_LINE) {
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

        // Format
        MutableText lineStart = Text.literal(Strings.repeat(" ", indent));
        lineStart.fillStyle(primaryStyle);
        List<Text> formattedLines = new ArrayList<>(lines.size());
        Couple<Style> styles = Couple.create(highlightStyle, primaryStyle);

        boolean currentlyHighlighted = false;
        for (String string : lines) {
            MutableText currentComponent = lineStart.copyContentOnly();
            String[] split = string.split("_", -1);
            for (String part : split) {
                currentComponent.append(Text.literal(part).fillStyle(styles.get(currentlyHighlighted)));
                currentlyHighlighted = !currentlyHighlighted;
            }

            formattedLines.add(currentComponent);
            currentlyHighlighted = !currentlyHighlighted;
        }

        return formattedLines;
    }
}
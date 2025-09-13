package com.zurrtum.create.client.flywheel.backend.glsl;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.client.flywheel.backend.glsl.span.CharPos;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import net.minecraft.util.Identifier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SourceLines implements CharSequence {
    private static final Pattern NEW_LINE = Pattern.compile("(\\r\\n|\\r|\\n)");
    public final Identifier name;
    private final IntList lineStarts;
    private final ImmutableList<String> lines;
    public final String raw;

    public SourceLines(Identifier name, String raw) {
        this.name = name;
        this.raw = raw;
        this.lineStarts = createLineLookup(raw);
        this.lines = getLines(raw, this.lineStarts);
    }

    public int count() {
        return this.lines.size();
    }

    public String lineString(int lineNo) {
        return (String) this.lines.get(lineNo);
    }

    public int lineStartIndex(int lineNo) {
        return this.lineStarts.getInt(lineNo);
    }

    public CharPos getCharPos(int charPos) {
        int lineNo;
        for (lineNo = 0; lineNo < this.lineStarts.size(); ++lineNo) {
            int ls = this.lineStarts.getInt(lineNo);
            if (charPos < ls) {
                break;
            }
        }

        --lineNo;
        int lineStart = this.lineStarts.getInt(lineNo);
        return new CharPos(charPos, lineNo, charPos - lineStart);
    }

    public String printLinesWithNumbers() {
        StringBuilder builder = new StringBuilder();
        int i = 0;

        for (int linesSize = this.lines.size(); i < linesSize; ++i) {
            builder.append(String.format("%1$4s: ", i + 1)).append((String) this.lines.get(i)).append('\n');
        }

        return builder.toString();
    }

    private static IntList createLineLookup(String source) {
        if (source.isEmpty()) {
            return IntLists.emptyList();
        } else {
            IntList l = new IntArrayList();
            l.add(0);
            Matcher matcher = NEW_LINE.matcher(source);

            while (matcher.find()) {
                l.add(matcher.end());
            }

            return l;
        }
    }

    private static ImmutableList<String> getLines(String source, IntList lines) {
        ImmutableList.Builder<String> builder = ImmutableList.builder();

        for (int i = 1; i < lines.size(); ++i) {
            int start = lines.getInt(i - 1);
            int end = lines.getInt(i);
            builder.add(source.substring(start, end).stripTrailing());
        }

        return builder.build();
    }

    public String toString() {
        return this.raw;
    }

    public CharSequence subSequence(int start, int end) {
        return this.raw.subSequence(start, end);
    }

    public char charAt(int i) {
        return this.raw.charAt(i);
    }

    public int length() {
        return this.raw.length();
    }

    public int lineWidth(int spanLine) {
        return ((String) this.lines.get(spanLine)).length();
    }

    public int lineStartColTrimmed(int line) {
        String lineString = this.lineString(line);
        int end = lineString.length();

        int col;
        for (col = 0; col < end && Character.isWhitespace(this.charAt(col)); ++col) {
        }

        return col;
    }

    public int lineStartPosTrimmed(int line) {
        return this.lineStartIndex(line) + this.lineStartColTrimmed(line);
    }
}

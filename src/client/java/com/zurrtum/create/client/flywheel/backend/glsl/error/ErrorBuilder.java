package com.zurrtum.create.client.flywheel.backend.glsl.error;

import com.zurrtum.create.client.flywheel.backend.glsl.SourceFile;
import com.zurrtum.create.client.flywheel.backend.glsl.SourceLines;
import com.zurrtum.create.client.flywheel.backend.glsl.error.lines.*;
import com.zurrtum.create.client.flywheel.backend.glsl.span.Span;
import com.zurrtum.create.client.flywheel.lib.util.StringUtil;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ErrorBuilder {
    @VisibleForTesting
    public static boolean CONSOLE_COLORS = true;
    private final List<ErrorLine> lines = new ArrayList();

    private ErrorBuilder() {
    }

    public static ErrorBuilder create() {
        return new ErrorBuilder();
    }

    public ErrorBuilder error(String msg) {
        return this.header(ErrorLevel.ERROR, msg);
    }

    public ErrorBuilder warn(String msg) {
        return this.header(ErrorLevel.WARN, msg);
    }

    public ErrorBuilder hint(String msg) {
        return this.header(ErrorLevel.HINT, msg);
    }

    public ErrorBuilder note(String msg) {
        return this.header(ErrorLevel.NOTE, msg);
    }

    public ErrorBuilder header(ErrorLevel level, String msg) {
        this.lines.add(new HeaderLine(level.toString(), msg));
        return this;
    }

    public ErrorBuilder extra(String msg) {
        this.lines.add(new TextLine(msg));
        return this;
    }

    public ErrorBuilder pointAtFile(SourceFile file) {
        return this.pointAtFile(file.name);
    }

    public ErrorBuilder pointAtFile(SourceLines source) {
        return this.pointAtFile(source.name);
    }

    public ErrorBuilder pointAtFile(Identifier file) {
        return this.pointAtFile(file.toString());
    }

    public ErrorBuilder pointAtFile(String file) {
        this.lines.add(new FileLine(file));
        return this;
    }

    public ErrorBuilder hintIncludeFor(@Nullable Span span, String msg) {
        if (span == null) {
            return this;
        } else {
            SourceLines source = span.source();
            String var10000 = String.valueOf(source.name);
            String builder = "add #use \"" + var10000 + "\" " + msg + "\n defined here:";
            this.header(ErrorLevel.HINT, builder);
            return this.pointAtFile(source).pointAt(span, 0);
        }
    }

    public ErrorBuilder pointAt(Span span) {
        return this.pointAt(span, 0);
    }

    public ErrorBuilder pointAt(Span span, int ctxLines) {
        if (span.lines() == 1) {
            SourceLines lines = span.source();
            int spanLine = span.firstLine();
            int firstCol = span.start().col();
            int lastCol = span.end().col();
            this.pointAtLine(lines, spanLine, ctxLines, firstCol, lastCol);
        }

        return this;
    }

    public ErrorBuilder pointAtLine(SourceLines lines, int spanLine, int ctxLines) {
        return this.pointAtLine(lines, spanLine, ctxLines, lines.lineStartColTrimmed(spanLine), lines.lineWidth(spanLine));
    }

    public ErrorBuilder pointAtLine(SourceLines lines, int spanLine, int ctxLines, int firstCol, int lastCol) {
        int firstLine = Math.max(0, spanLine - ctxLines);
        int lastLine = Math.min(lines.count() - 1, spanLine + ctxLines);

        for (int i = firstLine; i <= lastLine; ++i) {
            CharSequence line = lines.lineString(i);
            this.lines.add(SourceLine.numbered(i + 1, line.toString()));
            if (i == spanLine) {
                this.lines.add(new SpanHighlightLine(firstCol, lastCol));
            }
        }

        return this;
    }

    public String build() {
        Stream<String> lineStream = this.getLineStream();
        if (CONSOLE_COLORS) {
            lineStream = lineStream.map((line) -> line + "\u001b[0m");
        }

        return (String) lineStream.collect(Collectors.joining("\n"));
    }

    private Stream<String> getLineStream() {
        int maxMargin = this.calculateMargin();
        return this.lines.stream().map((line) -> addPaddingToLine(maxMargin, line));
    }

    private static String addPaddingToLine(int maxMargin, ErrorLine errorLine) {
        int neededMargin = errorLine.neededMargin();
        if (neededMargin >= 0) {
            String var10000 = StringUtil.repeatChar(' ', maxMargin - neededMargin);
            return var10000 + errorLine.build();
        } else {
            return errorLine.build();
        }
    }

    private int calculateMargin() {
        int maxMargin = -1;

        for (ErrorLine line : this.lines) {
            int neededMargin = line.neededMargin();
            if (neededMargin > maxMargin) {
                maxMargin = neededMargin;
            }
        }

        return maxMargin;
    }

    public void nested(ErrorBuilder err) {
        Stream var10000 = err.getLineStream().map(NestedLine::new);
        List var10001 = this.lines;
        Objects.requireNonNull(var10001);
        var10000.forEach(var10001::add);
    }
}
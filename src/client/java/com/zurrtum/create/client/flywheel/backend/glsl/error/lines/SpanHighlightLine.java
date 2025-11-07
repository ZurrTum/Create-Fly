package com.zurrtum.create.client.flywheel.backend.glsl.error.lines;

public class SpanHighlightLine implements ErrorLine {
    private final String line;

    public SpanHighlightLine(int firstCol, int lastCol) {
        this.line = generateUnderline(firstCol, lastCol);
    }

    public String right() {
        return this.line;
    }

    public static String generateUnderline(int firstCol, int lastCol) {
        return " ".repeat(Math.max(0, firstCol)) + "^".repeat(Math.max(0, lastCol - firstCol));
    }
}

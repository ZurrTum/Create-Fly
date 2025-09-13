package com.zurrtum.create.client.flywheel.backend.glsl.error.lines;

public record SourceLine(String number, String line) implements ErrorLine {
    public SourceLine(String number, String line) {
        this.number = number;
        this.line = line;
    }

    public static SourceLine numbered(int number, String line) {
        return new SourceLine(Integer.toString(number), line);
    }

    public String left() {
        return this.number;
    }

    public String right() {
        return this.line;
    }

    public String number() {
        return this.number;
    }

    public String line() {
        return this.line;
    }
}

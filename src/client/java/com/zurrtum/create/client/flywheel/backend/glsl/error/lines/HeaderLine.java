package com.zurrtum.create.client.flywheel.backend.glsl.error.lines;

public record HeaderLine(String level, CharSequence message) implements ErrorLine {
    @Override
    public int neededMargin() {
        return -1;
    }

    public String build() {
        return level + ": " + message;
    }
}

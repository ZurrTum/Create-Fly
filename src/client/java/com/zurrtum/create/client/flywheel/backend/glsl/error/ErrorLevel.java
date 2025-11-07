package com.zurrtum.create.client.flywheel.backend.glsl.error;

public enum ErrorLevel {
    WARN(ConsoleColors.YELLOW, "warn"),
    ERROR(ConsoleColors.RED, "error"),
    HINT(ConsoleColors.WHITE_BRIGHT, "hint"),
    NOTE(ConsoleColors.WHITE_BRIGHT, "note");

    private final String color;
    private final String error;

    private ErrorLevel(String color, String error) {
        this.color = color;
        this.error = error;
    }

    public String toString() {
        return ErrorBuilder.CONSOLE_COLORS ? this.color + this.error : this.error;
    }
}

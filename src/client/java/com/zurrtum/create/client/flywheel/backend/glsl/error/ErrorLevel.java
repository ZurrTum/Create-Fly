package com.zurrtum.create.client.flywheel.backend.glsl.error;

public enum ErrorLevel {
    WARN("\u001b[0;33m", "warn"), ERROR("\u001b[0;31m", "error"), HINT("\u001b[0;97m", "hint"), NOTE("\u001b[0;97m", "note");

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

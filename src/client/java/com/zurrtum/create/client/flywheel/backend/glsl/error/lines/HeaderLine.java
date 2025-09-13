package com.zurrtum.create.client.flywheel.backend.glsl.error.lines;

public record HeaderLine(String level, CharSequence message) implements ErrorLine {
    public HeaderLine(String level, CharSequence message) {
        this.level = level;
        this.message = message;
    }

    public int neededMargin() {
        return -1;
    }

    public String build() {
        String var10000 = this.level;
        return var10000 + ": " + String.valueOf(this.message);
    }

    public String level() {
        return this.level;
    }

    public CharSequence message() {
        return this.message;
    }
}

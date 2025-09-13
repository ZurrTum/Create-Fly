package com.zurrtum.create.client.flywheel.backend.glsl.error.lines;

public record TextLine(String msg) implements ErrorLine {
    public TextLine(String msg) {
        this.msg = msg;
    }

    public String build() {
        return this.msg;
    }

    public String msg() {
        return this.msg;
    }
}

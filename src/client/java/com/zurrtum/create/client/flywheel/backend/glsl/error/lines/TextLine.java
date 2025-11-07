package com.zurrtum.create.client.flywheel.backend.glsl.error.lines;

public record TextLine(String msg) implements ErrorLine {
    public String build() {
        return this.msg;
    }
}

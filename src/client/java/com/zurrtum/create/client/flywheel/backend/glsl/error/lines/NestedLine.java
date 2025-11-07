package com.zurrtum.create.client.flywheel.backend.glsl.error.lines;

public record NestedLine(String right) implements ErrorLine {
    @Override
    public String right() {
        return this.right;
    }

    @Override
    public Divider divider() {
        return Divider.EQUALS;
    }
}

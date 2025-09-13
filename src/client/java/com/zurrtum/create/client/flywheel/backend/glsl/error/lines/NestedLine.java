package com.zurrtum.create.client.flywheel.backend.glsl.error.lines;

public record NestedLine(String right) implements ErrorLine {
    public NestedLine(String right) {
        this.right = right;
    }

    public String right() {
        return this.right;
    }

    public Divider divider() {
        return Divider.EQUALS;
    }
}

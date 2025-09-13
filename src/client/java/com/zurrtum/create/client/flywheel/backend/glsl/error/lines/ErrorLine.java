package com.zurrtum.create.client.flywheel.backend.glsl.error.lines;

public interface ErrorLine {
    default int neededMargin() {
        return this.left().length();
    }

    default Divider divider() {
        return Divider.BAR;
    }

    default String build() {
        String var10000 = this.left();
        return var10000 + String.valueOf(this.divider()) + this.right();
    }

    default String left() {
        return "";
    }

    default String right() {
        return "";
    }
}

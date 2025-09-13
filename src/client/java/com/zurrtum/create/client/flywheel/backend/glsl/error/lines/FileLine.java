package com.zurrtum.create.client.flywheel.backend.glsl.error.lines;

public record FileLine(String fileName) implements ErrorLine {
    public FileLine(String fileName) {
        this.fileName = fileName;
    }

    public String left() {
        return "-";
    }

    public Divider divider() {
        return Divider.ARROW;
    }

    public String right() {
        return this.fileName;
    }

    public String fileName() {
        return this.fileName;
    }
}

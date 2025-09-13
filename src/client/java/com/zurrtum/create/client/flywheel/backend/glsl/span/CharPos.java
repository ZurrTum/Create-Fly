package com.zurrtum.create.client.flywheel.backend.glsl.span;

public record CharPos(int pos, int line, int col) {
    public CharPos(int pos, int line, int col) {
        this.pos = pos;
        this.line = line;
        this.col = col;
    }

    public int pos() {
        return this.pos;
    }

    public int line() {
        return this.line;
    }

    public int col() {
        return this.col;
    }
}

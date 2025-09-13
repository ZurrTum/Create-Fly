package com.zurrtum.create.client.flywheel.backend.glsl.span;

import com.zurrtum.create.client.flywheel.backend.glsl.SourceLines;

public class StringSpan extends Span {
    public StringSpan(SourceLines in, int start, int end) {
        super(in, start, end);
    }

    public Span subSpan(int from, int to) {
        return new StringSpan(this.in, this.start.pos() + from, this.start.pos() + to);
    }

    public String get() {
        return this.in.raw.substring(this.start.pos(), this.end.pos());
    }

    public boolean isErr() {
        return false;
    }
}

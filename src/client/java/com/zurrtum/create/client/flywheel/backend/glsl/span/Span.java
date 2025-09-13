package com.zurrtum.create.client.flywheel.backend.glsl.span;

import com.zurrtum.create.client.flywheel.backend.glsl.SourceLines;

import java.util.regex.Matcher;

public abstract class Span implements CharSequence, Comparable<Span> {
    protected final SourceLines in;
    protected final CharPos start;
    protected final CharPos end;

    public Span(SourceLines in, int start, int end) {
        this(in, in.getCharPos(start), in.getCharPos(end));
    }

    public Span(SourceLines in, CharPos start, CharPos end) {
        this.in = in;
        this.start = start;
        this.end = end;
    }

    public SourceLines source() {
        return this.in;
    }

    public CharPos start() {
        return this.start;
    }

    public CharPos end() {
        return this.end;
    }

    public int startIndex() {
        return this.start.pos();
    }

    public int endIndex() {
        return this.end.pos();
    }

    public boolean isEmpty() {
        return this.start == this.end;
    }

    public int lines() {
        return this.end.line() - this.start.line() + 1;
    }

    public int firstLine() {
        return this.start.line();
    }

    public abstract Span subSpan(int var1, int var2);

    public abstract String get();

    public abstract boolean isErr();

    public int length() {
        return this.endIndex() - this.startIndex();
    }

    public char charAt(int index) {
        return this.in.charAt(this.start.pos() + index);
    }

    public CharSequence subSequence(int start, int end) {
        return this.subSpan(start, end);
    }

    public String toString() {
        return this.get();
    }

    public static Span fromMatcher(SourceLines src, Matcher m, int group) {
        return new StringSpan(src, m.start(group), m.end(group));
    }

    public static Span fromMatcher(Span superSpan, Matcher m, int group) {
        return superSpan.subSpan(m.start(group), m.end(group));
    }

    public static Span fromMatcher(SourceLines src, Matcher m) {
        return new StringSpan(src, m.start(), m.end());
    }

    public static Span fromMatcher(Span superSpan, Matcher m) {
        return superSpan.subSpan(m.start(), m.end());
    }

    public int compareTo(Span o) {
        return Integer.compareUnsigned(this.startIndex(), o.startIndex());
    }
}

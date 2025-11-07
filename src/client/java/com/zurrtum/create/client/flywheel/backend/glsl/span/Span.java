package com.zurrtum.create.client.flywheel.backend.glsl.span;

import com.zurrtum.create.client.flywheel.backend.glsl.SourceFile;
import com.zurrtum.create.client.flywheel.backend.glsl.SourceLines;

import java.util.regex.Matcher;

/**
 * A segment of code in a {@link SourceFile}.
 *
 * <p>
 * Spans are used for pretty-printing errors.
 * </p>
 */
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

    /**
     * @return The file that contains this code segment.
     */
    public SourceLines source() {
        return this.in;
    }

    public CharPos start() {
        return this.start;
    }

    public CharPos end() {
        return this.end;
    }

    /**
     * @return the string index at the (inclusive) beginning of this code segment.
     */
    public int startIndex() {
        return this.start.pos();
    }

    /**
     * @return the string index at the (exclusive) end of this code segment.
     */
    public int endIndex() {
        return this.end.pos();
    }

    /**
     * @return true if this span has no width.
     */
    public boolean isEmpty() {
        return this.start == this.end;
    }

    /**
     * @return how many lines this span spans.
     */
    public int lines() {
        return this.end.line() - this.start.line() + 1;
    }

    public int firstLine() {
        return this.start.line();
    }

    /**
     * Get a span referring to a code segment inside this code segment.
     */
    public abstract Span subSpan(int var1, int var2);

    /**
     * @return the portion of code represented by this span.
     */
    public abstract String get();

    public abstract boolean isErr();

    @Override
    public int length() {
        return this.endIndex() - this.startIndex();
    }

    @Override
    public char charAt(int index) {
        return this.in.charAt(this.start.pos() + index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return this.subSpan(start, end);
    }

    @Override
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

    @Override
    public int compareTo(Span o) {
        return Integer.compareUnsigned(this.startIndex(), o.startIndex());
    }
}

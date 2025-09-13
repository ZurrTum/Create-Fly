package com.zurrtum.create.client.flywheel.backend.glsl;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.client.flywheel.backend.glsl.span.Span;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Import(Span self, Span file) {
    public static final Pattern PATTERN = Pattern.compile("^\\s*#\\s*include\\s+\"(.*)\"", 8);

    public Import(Span self, Span file) {
        this.self = self;
        this.file = file;
    }

    public static ImmutableList<Import> parseImports(SourceLines source) {
        Matcher matcher = PATTERN.matcher(source);
        ImmutableList.Builder<Import> imports = ImmutableList.builder();

        while (matcher.find()) {
            Span use = Span.fromMatcher(source, matcher);
            Span file = Span.fromMatcher(source, matcher, 1);
            imports.add(new Import(use, file));
        }

        return imports.build();
    }

    public Span self() {
        return this.self;
    }

    public Span file() {
        return this.file;
    }
}

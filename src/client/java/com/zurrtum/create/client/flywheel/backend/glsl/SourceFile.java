package com.zurrtum.create.client.flywheel.backend.glsl;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.zurrtum.create.client.flywheel.backend.glsl.span.Span;
import com.zurrtum.create.client.flywheel.backend.glsl.span.StringSpan;
import com.zurrtum.create.client.flywheel.lib.util.ResourceUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * Immutable class representing a shader file.
 *
 * <p>
 * This class parses shader files and generates what is effectively a high level AST of the source.
 * </p>
 */
public class SourceFile implements SourceComponent {
    public final Identifier name;

    public final SourceLines source;

    /**
     * Includes ordered as defined in the source.
     */
    public final ImmutableList<Import> imports;

    public final List<SourceFile> included;

    public final String finalSource;

    private SourceFile(Identifier name, SourceLines source, ImmutableList<Import> imports, List<SourceFile> included, String finalSource) {
        this.name = name;
        this.source = source;
        this.imports = imports;
        this.included = included;
        this.finalSource = finalSource;
    }

    public static LoadResult empty(Identifier name) {
        return new LoadResult.Success(new SourceFile(name, new SourceLines(name, ""), ImmutableList.of(), ImmutableList.of(), ""));
    }

    public static LoadResult parse(Function<Identifier, LoadResult> sourceFinder, Identifier name, String stringSource) {
        var source = new SourceLines(name, stringSource);

        var imports = Import.parseImports(source);

        List<SourceFile> included = new ArrayList<>();
        List<Pair<Span, LoadError>> failures = new ArrayList<>();

        Set<String> seen = new HashSet<>();
        for (Import i : imports) {
            var fileSpan = i.file();
            String string = fileSpan.toString();
            if (!seen.add(string)) {
                continue;
            }

            Identifier location;
            try {
                location = ResourceUtil.parseFlywheelDefault(string);
            } catch (InvalidIdentifierException e) {
                failures.add(Pair.of(fileSpan, new LoadError.MalformedInclude(e)));
                continue;
            }

            var result = sourceFinder.apply(location);

            if (result instanceof LoadResult.Success s) {
                included.add(s.unwrap());
            } else if (result instanceof LoadResult.Failure(LoadError error)) {
                failures.add(Pair.of(fileSpan, error));
            }
        }
        if (!failures.isEmpty()) {
            return new LoadResult.Failure(new LoadError.IncludeError(name, failures));
        }

        var finalSource = generateFinalSource(imports, source);
        return new LoadResult.Success(new SourceFile(name, source, imports, included, finalSource));
    }

    @Override
    public Collection<? extends SourceComponent> included() {
        return included;
    }

    @Override
    public String source() {
        return finalSource;
    }

    @Override
    public String name() {
        return name.toString();
    }

    public Span getLineSpanNoWhitespace(int line) {
        int begin = source.lineStartIndex(line);
        int end = begin + source.lineString(line).length();

        while (begin < end && Character.isWhitespace(source.charAt(begin))) {
            begin++;
        }

        return new StringSpan(source, begin, end);
    }

    public Span getLineSpanMatching(int line, @Nullable String match) {
        if (match == null) {
            return getLineSpanNoWhitespace(line);
        }

        var spanBegin = source.lineString(line).indexOf(match);

        if (spanBegin == -1) {
            return getLineSpanNoWhitespace(line);
        }

        int begin = source.lineStartIndex(line) + spanBegin;
        int end = begin + match.length();

        return new StringSpan(source, begin, end);
    }

    @Override
    public String toString() {
        return name.toString();
    }

    @Override
    public boolean equals(Object o) {
        // SourceFiles are only equal by reference.
        return this == o;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    private static String generateFinalSource(ImmutableList<Import> imports, SourceLines source) {
        var out = new StringBuilder();

        int lastEnd = 0;

        for (var include : imports) {
            var loc = include.self();

            out.append(source, lastEnd, loc.startIndex());

            lastEnd = loc.endIndex();
        }

        out.append(source, lastEnd, source.length());

        return out.toString();
    }
}

package com.zurrtum.create.client.flywheel.backend.compile.component;

import com.zurrtum.create.client.flywheel.backend.glsl.SourceComponent;
import com.zurrtum.create.client.flywheel.lib.util.ResourceUtil;

import java.util.Collection;
import java.util.Map;

public final class StringSubstitutionComponent implements SourceComponent {
    private final SourceComponent source;
    private final Map<String, String> replacements;
    private final String sourceString;

    public StringSubstitutionComponent(SourceComponent source, String find, String replace) {
        this(source, Map.of(find, replace));
    }

    public StringSubstitutionComponent(SourceComponent source, Map<String, String> replacements) {
        this.source = source;
        this.replacements = replacements;
        this.sourceString = source.source();
    }

    public String remapFnName(String name) {
        return replacements.getOrDefault(name, name);
    }

    public boolean replaces(String name) {
        return replacements.containsKey(name) && sourceString.contains(name);
    }

    @Override
    public String source() {
        var source = sourceString;

        for (var entry : replacements.entrySet()) {
            source = source.replace(entry.getKey(), entry.getValue());
        }

        return source;
    }

    @Override
    public String name() {
        return ResourceUtil.rl("string_substitution").toString() + " / " + source.name();
    }

    @Override
    public Collection<? extends SourceComponent> included() {
        return source.included();
    }
}

package com.zurrtum.create.client.flywheel.backend.glsl.generate;

import com.mojang.datafixers.util.Pair;
import com.zurrtum.create.client.flywheel.lib.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GlslUniformBlock implements GlslBuilder.Declaration {
    private String qualifier;
    private String name;
    private final List<Pair<String, String>> members = new ArrayList<>();

    public GlslUniformBlock layout(String qualifier) {
        this.qualifier = qualifier;
        return this;
    }

    public GlslUniformBlock name(String name) {
        this.name = name;
        return this;
    }

    public GlslUniformBlock member(String typeName, String variableName) {
        members.add(Pair.of(typeName, variableName));
        return this;
    }

    @Override
    public String prettyPrint() {
        return """
            layout(%s) uniform %s {
            %s
            };""".formatted(qualifier, name, StringUtil.indent(formatMembers(), 4));
    }

    private String formatMembers() {
        return members.stream().map(p -> p.getFirst() + " " + p.getSecond() + ";").collect(Collectors.joining("\n"));
    }
}

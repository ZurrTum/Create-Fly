package com.zurrtum.create.client.flywheel.backend.compile.component;

import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import com.zurrtum.create.client.flywheel.api.layout.Layout;
import com.zurrtum.create.client.flywheel.backend.compile.LayoutInterpreter;
import com.zurrtum.create.client.flywheel.backend.glsl.SourceComponent;
import com.zurrtum.create.client.flywheel.backend.glsl.generate.GlslBuilder;
import com.zurrtum.create.client.flywheel.lib.util.ResourceUtil;

import java.util.Collection;
import java.util.Collections;

public class InstanceStructComponent implements SourceComponent {
    private static final String STRUCT_NAME = "FlwInstance";

    private final Layout layout;

    public InstanceStructComponent(InstanceType<?> type) {
        layout = type.layout();
    }

    @Override
    public String name() {
        return ResourceUtil.rl("instance_struct").toString();
    }

    @Override
    public Collection<? extends SourceComponent> included() {
        return Collections.emptyList();
    }

    @Override
    public String source() {
        var builder = new GlslBuilder();

        var instance = builder.struct();
        instance.name(STRUCT_NAME);
        for (var element : layout.elements()) {
            instance.addField(LayoutInterpreter.typeName(element.type()), element.name());
        }

        builder.blankLine();
        return builder.build();
    }
}

package com.zurrtum.create.client.flywheel.backend.compile;

import com.zurrtum.create.client.flywheel.backend.Samplers;
import com.zurrtum.create.client.flywheel.backend.compile.component.BufferTextureInstanceComponent;
import com.zurrtum.create.client.flywheel.backend.compile.component.SsboInstanceComponent;
import com.zurrtum.create.client.flywheel.lib.util.ResourceUtil;

public final class Pipelines {
    public static final Pipeline INSTANCING = Pipeline.builder().compilerMarker("instancing")
        .vertexMain(ResourceUtil.rl("internal/instancing/main.vert")).fragmentMain(ResourceUtil.rl("internal/instancing/main.frag"))
        .assembler(BufferTextureInstanceComponent::new).onLink(program -> {
            program.setSamplerBinding("_flw_instances", Samplers.INSTANCE_BUFFER);
            program.setSamplerBinding("_flw_lightLut", Samplers.LIGHT_LUT);
            program.setSamplerBinding("_flw_lightSections", Samplers.LIGHT_SECTIONS);
        }).build();

    public static final Pipeline INDIRECT = Pipeline.builder().compilerMarker("indirect").vertexMain(ResourceUtil.rl("internal/indirect/main.vert"))
        .fragmentMain(ResourceUtil.rl("internal/indirect/main.frag")).assembler(SsboInstanceComponent::new).onLink($ -> {
        }).build();

    private Pipelines() {
    }
}

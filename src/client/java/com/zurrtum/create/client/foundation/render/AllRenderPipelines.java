package com.zurrtum.create.client.foundation.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllRenderPipelines {
    public static final Identifier GLOWING_ID = Identifier.fromNamespaceAndPath(MOD_ID, "core/glowing_shader");
    public static final RenderPipeline.Snippet GLOWING_SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
        .withVertexShader(GLOWING_ID).withFragmentShader(GLOWING_ID).withSampler("Sampler0").withSampler("Sampler2")
        .withVertexFormat(DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS).buildSnippet();
    public static final RenderPipeline ADDITIVE = register(
        "additive",
        RenderPipeline.builder(RenderPipelines.BLOCK_SNIPPET).withBlend(BlendFunction.ADDITIVE).withCull(false)
    );
    public static final RenderPipeline ADDITIVE2 = register(
        "additive2",
        RenderPipeline.builder(RenderPipelines.BLOCK_SNIPPET).withBlend(BlendFunction.ADDITIVE).withCull(false)
            .withDepthWrite(false)
    );
    public static final RenderPipeline GLOWING = register(
        "glowing", RenderPipeline.builder(GLOWING_SNIPPET).withBlend(new BlendFunction(
            SourceFactor.SRC_ALPHA,
            DestFactor.ONE_MINUS_SRC_ALPHA,
            SourceFactor.SRC_ALPHA,
            DestFactor.ONE_MINUS_SRC_ALPHA
        ))
    );
    public static final RenderPipeline GLOWING_TRANSLUCENT = register(
        "glowing_translucent",
        RenderPipeline.builder(GLOWING_SNIPPET).withBlend(BlendFunction.TRANSLUCENT)
    );
    public static final RenderPipeline CUBE = register(
        "cube",
        RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET).withBlend(new BlendFunction(
            SourceFactor.SRC_ALPHA,
            DestFactor.ONE_MINUS_SRC_ALPHA,
            SourceFactor.SRC_ALPHA,
            DestFactor.ONE_MINUS_SRC_ALPHA
        )).withDepthWrite(false)
    );

    private static RenderPipeline register(String id, RenderPipeline.Builder builder) {
        Identifier location = Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/" + id);
        RenderPipeline pipeline = builder.withLocation(location).build();
        RenderPipelines.PIPELINES_BY_LOCATION.put(location, pipeline);
        return pipeline;
    }
}

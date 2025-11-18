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
    public static final RenderPipeline ADDITIVE = RenderPipeline.builder(RenderPipelines.BLOCK_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/additive")).withBlend(BlendFunction.ADDITIVE).withCull(false).build();
    public static final RenderPipeline ADDITIVE2 = RenderPipeline.builder(RenderPipelines.BLOCK_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/additive2")).withBlend(BlendFunction.ADDITIVE).withCull(false)
        .withDepthWrite(false).build();
    public static final RenderPipeline GLOWING = RenderPipeline.builder(GLOWING_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/glowing"))
        .withBlend(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA))
        .build();
    public static final RenderPipeline GLOWING_TRANSLUCENT = RenderPipeline.builder(GLOWING_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/glowing_translucent")).withBlend(BlendFunction.TRANSLUCENT).build();
    public static final RenderPipeline CUBE = RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/cube"))
        .withBlend(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA))
        .withDepthWrite(false).build();
}

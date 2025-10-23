package com.zurrtum.create.client.foundation.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllRenderPipelines {
    public static final Identifier GLOWING_ID = Identifier.of(MOD_ID, "core/glowing_shader");
    public static final RenderPipeline.Snippet GLOWING_SNIPPET = RenderPipeline.builder(RenderPipelines.TRANSFORMS_PROJECTION_FOG_SNIPPET)
        .withVertexShader(GLOWING_ID).withFragmentShader(GLOWING_ID).withSampler("Sampler0").withSampler("Sampler2")
        .withVertexFormat(VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS).buildSnippet();
    public static final RenderPipeline ADDITIVE = RenderPipeline.builder(RenderPipelines.TERRAIN_SNIPPET)
        .withLocation(Identifier.of(MOD_ID, "pipeline/additive")).withBlend(BlendFunction.ADDITIVE).withCull(false).build();
    public static final RenderPipeline ADDITIVE2 = RenderPipeline.builder(RenderPipelines.TERRAIN_SNIPPET)
        .withLocation(Identifier.of(MOD_ID, "pipeline/additive2")).withBlend(BlendFunction.ADDITIVE).withCull(false).withDepthWrite(false).build();
    public static final RenderPipeline GLOWING = RenderPipeline.builder(GLOWING_SNIPPET).withLocation(Identifier.of(MOD_ID, "pipeline/glowing"))
        .withBlend(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA))
        .build();
    public static final RenderPipeline GLOWING_TRANSLUCENT = RenderPipeline.builder(GLOWING_SNIPPET)
        .withLocation(Identifier.of(MOD_ID, "pipeline/glowing_translucent")).withBlend(BlendFunction.TRANSLUCENT).build();
    public static final RenderPipeline CUBE = RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
        .withLocation(Identifier.of(MOD_ID, "pipeline/cube"))
        .withBlend(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA))
        .withDepthWrite(false).build();
}

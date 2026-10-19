package com.zurrtum.create.client.foundation.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.*;
import com.zurrtum.create.client.catnip.render.PonderRenderPipelines;
import com.zurrtum.create.client.ponder.Ponder;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.oit.OitPipelineSet;
import net.minecraft.resources.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllRenderPipelines {
    public static final Identifier GLOWING_ID = Identifier.fromNamespaceAndPath(MOD_ID, "core/glowing_shader");
    public static final RenderPipeline.Snippet GLOWING_SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
        .withVertexShader(GLOWING_ID).withFragmentShader(GLOWING_ID)
        .withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER2).withVertexBinding(0, DefaultVertexFormat.BLOCK)
        .withPrimitiveTopology(PrimitiveTopology.QUADS).withDepthStencilState(DepthStencilState.DEFAULT).buildSnippet();
    public static final RenderPipeline.Snippet ADDITIVE_SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
        .withVertexShader("core/entity").withFragmentShader("core/entity").withShaderDefine("ALPHA_CUTOUT", 0.1F)
        .withShaderDefine("EMISSIVE").withShaderDefine("NO_OVERLAY").withShaderDefine("NO_CARDINAL_LIGHTING")
        .withBindGroupLayout(BindGroupLayouts.SAMPLER0).withCull(false).withVertexBinding(0, DefaultVertexFormat.ENTITY)
        .withPrimitiveTopology(PrimitiveTopology.QUADS).buildSnippet();
    public static final RenderPipeline ADDITIVE = RenderPipelines.register(RenderPipeline.builder(ADDITIVE_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/additive"))
        .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
        .withDepthStencilState(DepthStencilState.DEFAULT).build());
    public static final OitPipelineSet OIT_ADDITIVE = RenderPipelines.register(OitPipelineSet.builder(
        Ponder.MOD_ID + "/oit_additive",
        RenderPipeline.builder(ADDITIVE_SNIPPET).withShaderDefine("OIT_ADDITIVE")
    ).build());
    public static final RenderPipeline GLOWING = RenderPipelines.register(RenderPipeline.builder(GLOWING_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/glowing"))
        .withColorTargetState(ColorTargetState.DEFAULT).build());
    public static final RenderPipeline GLOWING_TRANSLUCENT = RenderPipelines.register(RenderPipeline.builder(
            GLOWING_SNIPPET).withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/glowing_translucent"))
        .withShaderDefine("ALPHA_CUTOUT", 0.1F).withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .build());
    public static final OitPipelineSet OIT_GLOWING = RenderPipelines.register(OitPipelineSet.builder(
        MOD_ID + "/oit_glowing",
        RenderPipeline.builder().withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
            .withBindGroupLayout(BindGroupLayouts.FOG).withBindGroupLayout(BindGroupLayouts.SAMPLER0)
            .withVertexShader(GLOWING_ID).withFragmentShader(GLOWING_ID).withVertexBinding(0, DefaultVertexFormat.BLOCK)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withDepthStencilState(PonderRenderPipelines.DEFAULT_TEST_NOT_WRITE).withShaderDefine("ALPHA_CUTOUT", 0.1F)
    ).withAccumulateModifier(accumulate -> accumulate.withBindGroupLayout(BindGroupLayouts.SAMPLER2)).build());
    public static final RenderPipeline CUBE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/cube"))
        .withColorTargetState(new ColorTargetState(new BlendFunction(
            BlendFactor.SRC_ALPHA,
            BlendFactor.ONE_MINUS_SRC_ALPHA,
            BlendFactor.SRC_ALPHA,
            BlendFactor.ONE_MINUS_SRC_ALPHA
        ))).withDepthStencilState(PonderRenderPipelines.DEFAULT_TEST_NOT_WRITE).build());
}

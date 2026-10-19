package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.*;
import com.mojang.renderpearl.api.pipeline.RenderPipeline.Snippet;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.oit.OitPipelineSet;
import net.minecraft.resources.Identifier;

import java.util.Optional;

import static com.zurrtum.create.client.ponder.Ponder.MOD_ID;

public class PonderRenderPipelines {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static final Optional<DepthStencilState> DEFAULT_TEST_NOT_WRITE = Optional.of(new DepthStencilState(
        CompareOp.GREATER_THAN_OR_EQUAL,
        false
    ));
    private static final Identifier ENTITY_BLOCK_ID = Identifier.fromNamespaceAndPath(MOD_ID, "entity_block");
    public static final Snippet ENTITY_BLOCK_NO_FORMAT_SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
        .withVertexShader(ENTITY_BLOCK_ID).withFragmentShader(ENTITY_BLOCK_ID)
        .withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER2).withPrimitiveTopology(PrimitiveTopology.QUADS)
        .withDepthStencilState(DepthStencilState.DEFAULT).buildSnippet();
    public static final Snippet OIT_ENTITY_BLOCK_NO_FORMAT_SNIPPET = RenderPipeline.builder()
        .withVertexShader(ENTITY_BLOCK_ID).withFragmentShader(ENTITY_BLOCK_ID)
        .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS).withBindGroupLayout(BindGroupLayouts.FOG)
        .withBindGroupLayout(BindGroupLayouts.SAMPLER0).withPrimitiveTopology(PrimitiveTopology.QUADS)
        .withDepthStencilState(DEFAULT_TEST_NOT_WRITE).withShaderDefine("ALPHA_CUTOUT", 0.1F).buildSnippet();
    public static final Snippet ENTITY_BLOCK_SNIPPET = RenderPipeline.builder(ENTITY_BLOCK_NO_FORMAT_SNIPPET)
        .withVertexBinding(0, DefaultVertexFormat.ENTITY).buildSnippet();
    public static final Snippet ENTITY_BLOCK_LIGHT_SNIPPET = RenderPipeline.builder(ENTITY_BLOCK_NO_FORMAT_SNIPPET)
        .withVertexBinding(0, DefaultVertexFormat.BLOCK).buildSnippet();
    public static final Snippet OIT_ENTITY_BLOCK_SNIPPET = RenderPipeline.builder(OIT_ENTITY_BLOCK_NO_FORMAT_SNIPPET)
        .withVertexBinding(0, DefaultVertexFormat.ENTITY).buildSnippet();
    public static final Snippet OIT_ENTITY_BLOCK_LIGHT_SNIPPET = RenderPipeline.builder(
        OIT_ENTITY_BLOCK_NO_FORMAT_SNIPPET).withVertexBinding(0, DefaultVertexFormat.BLOCK).buildSnippet();
    public static final RenderPipeline ENTITY_BLOCK_SOLID = RenderPipelines.register(RenderPipeline.builder(
            ENTITY_BLOCK_SNIPPET).withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/entity_block_solid"))
        .withShaderDefine("OVERWORLD").withColorTargetState(ColorTargetState.DEFAULT).build());
    public static final RenderPipeline ENTITY_BLOCK_CUTOUT = RenderPipelines.register(RenderPipeline.builder(
            ENTITY_BLOCK_SNIPPET).withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/entity_block_cutout"))
        .withShaderDefine("OVERWORLD").withShaderDefine("ALPHA_CUTOUT", 0.5F)
        .withColorTargetState(ColorTargetState.DEFAULT).build());
    public static final RenderPipeline ENTITY_BLOCK_TRANSLUCENT = RenderPipelines.register(RenderPipeline.builder(
            ENTITY_BLOCK_SNIPPET).withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/entity_block_translucent"))
        .withShaderDefine("OVERWORLD").withShaderDefine("ALPHA_CUTOUT", 0.1F)
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).build());
    public static final OitPipelineSet OIT_ENTITY_BLOCK_TRANSLUCENT = RenderPipelines.register(OitPipelineSet.builder(
        MOD_ID + "/entity_block_translucent",
        RenderPipeline.builder(OIT_ENTITY_BLOCK_SNIPPET).withShaderDefine("OVERWORLD")
    ).withAccumulateModifier(accumulate -> accumulate.withBindGroupLayout(BindGroupLayouts.SAMPLER2)).build());
    public static final RenderPipeline ENTITY_BLOCK_LIGHT_SOLID = RenderPipelines.register(RenderPipeline.builder(
            ENTITY_BLOCK_LIGHT_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/entity_block_light_solid"))
        .withColorTargetState(ColorTargetState.DEFAULT).build());
    public static final RenderPipeline ENTITY_BLOCK_LIGHT_CUTOUT = RenderPipelines.register(RenderPipeline.builder(
            ENTITY_BLOCK_LIGHT_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/entity_block_light_cutout"))
        .withShaderDefine("ALPHA_CUTOUT", 0.5F).withColorTargetState(ColorTargetState.DEFAULT).build());
    public static final RenderPipeline ENTITY_BLOCK_LIGHT_TRANSLUCENT = RenderPipelines.register(RenderPipeline.builder(
            ENTITY_BLOCK_LIGHT_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/entity_block_light_translucent"))
        .withShaderDefine("ALPHA_CUTOUT", 0.1F).withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .build());
    public static final OitPipelineSet OIT_ENTITY_BLOCK_LIGHT_TRANSLUCENT = RenderPipelines.register(OitPipelineSet.builder(MOD_ID + "/entity_block_light_translucent",
        RenderPipeline.builder(OIT_ENTITY_BLOCK_LIGHT_SNIPPET)
    ).withAccumulateModifier(accumulate -> accumulate.withBindGroupLayout(BindGroupLayouts.SAMPLER2)).build());
    public static final RenderPipeline NETHER_ENTITY_BLOCK_SOLID = RenderPipelines.register(RenderPipeline.builder(
            ENTITY_BLOCK_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/nether_entity_block_solid"))
        .withShaderDefine("NETHER").withColorTargetState(ColorTargetState.DEFAULT).build());
    public static final RenderPipeline NETHER_ENTITY_BLOCK_CUTOUT = RenderPipelines.register(RenderPipeline.builder(
            ENTITY_BLOCK_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/nether_entity_block_cutout"))
        .withShaderDefine("NETHER").withShaderDefine("ALPHA_CUTOUT", 0.5F)
        .withColorTargetState(ColorTargetState.DEFAULT).build());
    public static final RenderPipeline NETHER_ENTITY_BLOCK_TRANSLUCENT = RenderPipelines.register(RenderPipeline.builder(
            ENTITY_BLOCK_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/nether_entity_block_translucent"))
        .withShaderDefine("NETHER").withShaderDefine("ALPHA_CUTOUT", 0.1F)
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).build());
    public static final OitPipelineSet OIT_NETHER_ENTITY_BLOCK_TRANSLUCENT = RenderPipelines.register(OitPipelineSet.builder(
        MOD_ID + "/nether_entity_block_translucent",
        RenderPipeline.builder(OIT_ENTITY_BLOCK_SNIPPET).withShaderDefine("NETHER")
    ).withAccumulateModifier(accumulate -> accumulate.withBindGroupLayout(BindGroupLayouts.SAMPLER2)).build());
    public static final RenderPipeline NETHER_ENTITY_BLOCK_LIGHT_SOLID = RenderPipelines.register(RenderPipeline.builder(
            ENTITY_BLOCK_LIGHT_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/nether_entity_block_light_solid"))
        .withShaderDefine("NETHER_LIGHT").withColorTargetState(ColorTargetState.DEFAULT).build());
    public static final RenderPipeline NETHER_ENTITY_BLOCK_LIGHT_CUTOUT = RenderPipelines.register(RenderPipeline.builder(
            ENTITY_BLOCK_LIGHT_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/nether_entity_block_light_cutout"))
        .withShaderDefine("NETHER_LIGHT").withShaderDefine("ALPHA_CUTOUT", 0.5F)
        .withColorTargetState(ColorTargetState.DEFAULT).build());
    public static final RenderPipeline NETHER_ENTITY_BLOCK_LIGHT_TRANSLUCENT = RenderPipelines.register(RenderPipeline.builder(
            ENTITY_BLOCK_LIGHT_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/nether_entity_block_light_translucent"))
        .withShaderDefine("NETHER_LIGHT").withShaderDefine("ALPHA_CUTOUT", 0.1F)
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).build());
    public static final OitPipelineSet OIT_NETHER_ENTITY_BLOCK_LIGHT_TRANSLUCENT = RenderPipelines.register(
        OitPipelineSet.builder(
            MOD_ID + "/nether_entity_block_light_translucent",
            RenderPipeline.builder(OIT_ENTITY_BLOCK_LIGHT_SNIPPET).withShaderDefine("NETHER_LIGHT")
        ).withAccumulateModifier(accumulate -> accumulate.withBindGroupLayout(BindGroupLayouts.SAMPLER2)).build());
    public static final RenderPipeline GUI = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/gui"))
        .withDepthStencilState(DEFAULT_TEST_NOT_WRITE).build());
    public static final RenderPipeline ENTITY_TRANSLUCENT_CULL = RenderPipelines.register(RenderPipeline.builder(
            RenderPipelines.ENTITY_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/entity_translucent_cull"))
        .withShaderDefine("ALPHA_CUTOUT", 0.1F).withBindGroupLayout(BindGroupLayouts.SAMPLER1)
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .withDepthStencilState(DEFAULT_TEST_NOT_WRITE).build());
    public static final RenderPipeline ENTITY_TRANSLUCENT = RenderPipelines.register(RenderPipeline.builder(
            RenderPipelines.ENTITY_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/entity_translucent"))
        .withShaderDefine("ALPHA_CUTOUT", 0.1F).withShaderDefine("PER_FACE_LIGHTING")
        .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).withCull(false)
        .withDepthStencilState(DEFAULT_TEST_NOT_WRITE).build());
    public static final RenderPipeline TRIANGLE_FAN = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/triangle_fan"))
        .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR).withPrimitiveTopology(PrimitiveTopology.TRIANGLE_FAN)
        .build());
    public static final RenderPipeline POSITION_COLOR_TRIANGLES = RenderPipelines.register(RenderPipeline.builder(
            RenderPipelines.GLOBALS_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/position_color_triangles"))
        .withBindGroupLayout(BindGroupLayouts.PROJECTION).withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
        .withVertexShader("core/position_color").withFragmentShader("core/position_color")
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR).withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
        .withCull(false).build());
    public static final RenderPipeline POSITION_COLOR_STRIP = RenderPipelines.register(RenderPipeline.builder(
            RenderPipelines.DEBUG_FILLED_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/position_color_strip"))
        .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
        .withPrimitiveTopology(PrimitiveTopology.TRIANGLE_STRIP).build());
}

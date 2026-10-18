package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.*;
import com.mojang.renderpearl.api.pipeline.RenderPipeline.Snippet;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
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
    public static final Snippet ENTITY_BLOCK_SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
        .withVertexShader(ENTITY_BLOCK_ID).withFragmentShader(ENTITY_BLOCK_ID)
        .withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER2).withVertexBinding(0, DefaultVertexFormat.ENTITY)
        .withPrimitiveTopology(PrimitiveTopology.QUADS).withDepthStencilState(DepthStencilState.DEFAULT).buildSnippet();
    public static final Snippet ENTITY_BLOCK_LIGHT_SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
        .withVertexShader(ENTITY_BLOCK_ID).withFragmentShader(ENTITY_BLOCK_ID)
        .withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER2).withVertexBinding(0, DefaultVertexFormat.BLOCK)
        .withPrimitiveTopology(PrimitiveTopology.QUADS).withDepthStencilState(DepthStencilState.DEFAULT).buildSnippet();
    public static final RenderPipeline ENTITY_BLOCK_SOLID = register(
        "entity_block_solid",
        RenderPipeline.builder(ENTITY_BLOCK_SNIPPET).withShaderDefine("OVERWORLD")
    );
    public static final RenderPipeline ENTITY_BLOCK_CUTOUT = register(
        "entity_block_cutout",
        RenderPipeline.builder(ENTITY_BLOCK_SNIPPET).withShaderDefine("OVERWORLD")
            .withShaderDefine("ALPHA_CUTOUT", 0.5F)
    );
    public static final RenderPipeline ENTITY_BLOCK_TRANSLUCENT = register(
        "entity_block_translucent",
        RenderPipeline.builder(ENTITY_BLOCK_SNIPPET).withShaderDefine("OVERWORLD")
            .withShaderDefine("ALPHA_CUTOUT", 0.01F)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
    );
    public static final RenderPipeline ENTITY_BLOCK_LIGHT_SOLID = register(
        "entity_block_light_solid",
        RenderPipeline.builder(ENTITY_BLOCK_LIGHT_SNIPPET)
    );
    public static final RenderPipeline ENTITY_BLOCK_LIGHT_CUTOUT = register(
        "entity_block_light_cutout",
        RenderPipeline.builder(ENTITY_BLOCK_LIGHT_SNIPPET).withShaderDefine("ALPHA_CUTOUT", 0.5F)
    );
    public static final RenderPipeline ENTITY_BLOCK_LIGHT_TRANSLUCENT = register(
        "entity_block_light_translucent",
        RenderPipeline.builder(ENTITY_BLOCK_LIGHT_SNIPPET).withShaderDefine("ALPHA_CUTOUT", 0.01F)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
    );
    public static final RenderPipeline NETHER_ENTITY_BLOCK_SOLID = register(
        "nether_entity_block_solid",
        RenderPipeline.builder(ENTITY_BLOCK_SNIPPET).withShaderDefine("NETHER")
    );
    public static final RenderPipeline NETHER_ENTITY_BLOCK_CUTOUT = register(
        "nether_entity_block_cutout",
        RenderPipeline.builder(ENTITY_BLOCK_SNIPPET).withShaderDefine("NETHER").withShaderDefine("ALPHA_CUTOUT", 0.5F)
    );
    public static final RenderPipeline NETHER_ENTITY_BLOCK_TRANSLUCENT = register(
        "nether_entity_block_translucent",
        RenderPipeline.builder(ENTITY_BLOCK_SNIPPET).withShaderDefine("NETHER").withShaderDefine("ALPHA_CUTOUT", 0.01F)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
    );
    public static final RenderPipeline NETHER_ENTITY_BLOCK_LIGHT_SOLID = register(
        "nether_entity_block_light_solid",
        RenderPipeline.builder(ENTITY_BLOCK_LIGHT_SNIPPET).withShaderDefine("NETHER_LIGHT")
    );
    public static final RenderPipeline NETHER_ENTITY_BLOCK_LIGHT_CUTOUT = register(
        "nether_entity_block_light_cutout",
        RenderPipeline.builder(ENTITY_BLOCK_LIGHT_SNIPPET).withShaderDefine("NETHER_LIGHT")
            .withShaderDefine("ALPHA_CUTOUT", 0.5F)
    );
    public static final RenderPipeline NETHER_ENTITY_BLOCK_LIGHT_TRANSLUCENT = register(
        "nether_entity_block_light_translucent",
        RenderPipeline.builder(ENTITY_BLOCK_LIGHT_SNIPPET).withShaderDefine("NETHER_LIGHT")
            .withShaderDefine("ALPHA_CUTOUT", 0.01F)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
    );
    public static final RenderPipeline GUI = register(
        "gui",
        RenderPipeline.builder(RenderPipelines.GUI_SNIPPET).withDepthStencilState(DEFAULT_TEST_NOT_WRITE)
    );
    public static final RenderPipeline ENTITY_TRANSLUCENT_CULL = register(
        "entity_translucent_cull",
        RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET).withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(DEFAULT_TEST_NOT_WRITE)
    );
    public static final RenderPipeline ENTITY_TRANSLUCENT = register(
        "entity_translucent",
        RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET).withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).withCull(false)
            .withDepthStencilState(DEFAULT_TEST_NOT_WRITE)
    );
    public static final RenderPipeline TRIANGLE_FAN = register(
        "triangle_fan",
        RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLE_FAN)
    );
    public static final RenderPipeline POSITION_COLOR_TRIANGLES = register(
        "position_color_triangles",
        RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
            .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION).withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR).withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
            .withCull(false)
    );
    public static final RenderPipeline POSITION_COLOR_STRIP = register(
        "position_color_strip",
        RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLE_STRIP)
    );

    private static RenderPipeline register(String id, RenderPipeline.Builder builder) {
        Identifier location = Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/" + id);
        RenderPipeline pipeline = builder.withLocation(location).build();
        RenderPipelines.PIPELINES_BY_LOCATION.put(location, pipeline);
        return pipeline;
    }
}

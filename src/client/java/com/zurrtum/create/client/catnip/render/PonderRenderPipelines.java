package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.zurrtum.create.client.catnip.gui.IndexRenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import static com.zurrtum.create.client.ponder.Ponder.MOD_ID;

public class PonderRenderPipelines {
    public static final RenderPipeline RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_LIGHT_DIR_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/item_entity_translucent_cull"))
        .withVertexShader("core/rendertype_item_entity_translucent_cull").withFragmentShader("core/rendertype_item_entity_translucent_cull")
        .withSampler("Sampler0").withSampler("Sampler2").withBlend(BlendFunction.TRANSLUCENT)
        .withVertexFormat(DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS).withDepthWrite(false).build();
    public static final RenderPipeline ENTITY_TRANSLUCENT = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/entity_translucent")).withShaderDefine("ALPHA_CUTOUT", 0.1F)
        .withSampler("Sampler1").withBlend(BlendFunction.TRANSLUCENT).withCull(false).withDepthWrite(false).build();
    public static final RenderPipeline TRIANGLE_FAN = IndexRenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/triangle_fan"))
        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_FAN).build();
    public static final RenderPipeline POSITION_COLOR_TRIANGLES = IndexRenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/position_color_triangles"))
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withCull(false)
        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES).build();
    public static final RenderPipeline POSITION_COLOR_STRIP = IndexRenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/position_color_strip"))
        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP).build();
    public static final RenderPipeline BLIT_SCREEN = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/blit_screen")).withVertexShader("core/blit_screen")
        .withFragmentShader("core/blit_screen").withSampler("InSampler").withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS).build();
}

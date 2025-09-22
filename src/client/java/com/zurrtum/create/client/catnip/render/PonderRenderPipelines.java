package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

import static com.zurrtum.create.client.ponder.Ponder.MOD_ID;

public class PonderRenderPipelines {
    public static final RenderPipeline RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL = RenderPipeline.builder(RenderPipelines.TRANSFORMS_PROJECTION_FOG_LIGHTING_SNIPPET)
        .withLocation(Identifier.of(MOD_ID, "pipeline/item_entity_translucent_cull")).withVertexShader("core/rendertype_item_entity_translucent_cull")
        .withFragmentShader("core/rendertype_item_entity_translucent_cull").withSampler("Sampler0").withSampler("Sampler2")
        .withBlend(BlendFunction.TRANSLUCENT).withVertexFormat(VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS)
        .withDepthWrite(false).build();
    public static final RenderPipeline ENTITY_TRANSLUCENT = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
        .withLocation(Identifier.of(MOD_ID, "pipeline/entity_translucent")).withShaderDefine("ALPHA_CUTOUT", 0.1F).withSampler("Sampler1")
        .withBlend(BlendFunction.TRANSLUCENT).withCull(false).withDepthWrite(false).build();
}

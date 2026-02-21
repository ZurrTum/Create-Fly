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
    public static final RenderPipeline GUI = register(
        "gui",
        RenderPipeline.builder(RenderPipelines.GUI_SNIPPET).withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
    );
    public static final RenderPipeline RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL = register(
        "item_entity_translucent_cull",
        RenderPipeline.builder(RenderPipelines.MATRICES_FOG_LIGHT_DIR_SNIPPET)
            .withVertexShader("core/rendertype_item_entity_translucent_cull")
            .withFragmentShader("core/rendertype_item_entity_translucent_cull").withSampler("Sampler0")
            .withSampler("Sampler2").withBlend(BlendFunction.TRANSLUCENT)
            .withVertexFormat(DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS).withDepthWrite(false)
    );
    public static final RenderPipeline ENTITY_TRANSLUCENT = register(
        "entity_translucent",
        RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET).withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withSampler("Sampler1").withBlend(BlendFunction.TRANSLUCENT).withCull(false).withDepthWrite(false)
    );
    public static final RenderPipeline TRIANGLE_FAN = register(
        "triangle_fan",
        IndexRenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_FAN)
    );
    public static final RenderPipeline POSITION_COLOR_TRIANGLES = register(
        "position_color_triangles",
        IndexRenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
    );
    public static final RenderPipeline POSITION_COLOR_STRIP = register(
        "position_color_strip",
        IndexRenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
    );

    private static RenderPipeline register(String id, RenderPipeline.Builder builder) {
        Identifier location = Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/" + id);
        RenderPipeline pipeline = builder.withLocation(location).build();
        RenderPipelines.PIPELINES_BY_LOCATION.put(location, pipeline);
        return pipeline;
    }
}

package com.zurrtum.create.client.foundation.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.*;
import com.zurrtum.create.client.catnip.render.PonderRenderPipelines;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllRenderPipelines {
    public static final Identifier GLOWING_ID = Identifier.fromNamespaceAndPath(MOD_ID, "core/glowing_shader");
    public static final RenderPipeline.Snippet GLOWING_SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
        .withVertexShader(GLOWING_ID).withFragmentShader(GLOWING_ID)
        .withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER2).withVertexBinding(0, DefaultVertexFormat.ENTITY)
        .withPrimitiveTopology(PrimitiveTopology.QUADS).withDepthStencilState(DepthStencilState.DEFAULT).buildSnippet();
    public static final RenderPipeline ADDITIVE = register(
        "additive",
        RenderPipeline.builder(RenderPipelines.BLOCK_SNIPPET)
            .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE)).withCull(false)
    );
    public static final RenderPipeline ADDITIVE2 = CustomRenderPipeline.markSolidBlend(register(
        "additive2",
        RenderPipeline.builder(RenderPipelines.BLOCK_SNIPPET)
            .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE)).withCull(false)
            .withDepthStencilState(PonderRenderPipelines.DEFAULT_TEST_NOT_WRITE)
    ));
    public static final RenderPipeline GLOWING = CustomRenderPipeline.markSolidBlend(register(
        "glowing", RenderPipeline.builder(GLOWING_SNIPPET).withColorTargetState(new ColorTargetState(new BlendFunction(
            BlendFactor.SRC_ALPHA,
            BlendFactor.ONE_MINUS_SRC_ALPHA,
            BlendFactor.SRC_ALPHA,
            BlendFactor.ONE_MINUS_SRC_ALPHA
        )))
    ));
    public static final RenderPipeline GLOWING_TRANSLUCENT = register(
        "glowing_translucent",
        RenderPipeline.builder(GLOWING_SNIPPET).withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
    );
    public static final RenderPipeline CUBE = register(
        "cube",
        RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
            .withColorTargetState(new ColorTargetState(new BlendFunction(
                BlendFactor.SRC_ALPHA,
                BlendFactor.ONE_MINUS_SRC_ALPHA,
                BlendFactor.SRC_ALPHA,
                BlendFactor.ONE_MINUS_SRC_ALPHA
            ))).withDepthStencilState(PonderRenderPipelines.DEFAULT_TEST_NOT_WRITE)
    );

    private static RenderPipeline register(String id, RenderPipeline.Builder builder) {
        Identifier location = Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/" + id);
        RenderPipeline pipeline = builder.withLocation(location).build();
        RenderPipelines.PIPELINES_BY_LOCATION.put(location, pipeline);
        return pipeline;
    }
}

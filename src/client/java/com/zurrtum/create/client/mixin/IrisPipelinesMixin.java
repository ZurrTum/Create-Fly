package com.zurrtum.create.client.mixin;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.zurrtum.create.client.catnip.render.PonderRenderPipelines;
import com.zurrtum.create.client.foundation.render.AllRenderPipelines;
import it.unimi.dsi.fastutil.Function;
import net.irisshaders.iris.pipeline.IrisPipelines;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IrisPipelines.class)
public abstract class IrisPipelinesMixin {
    @Shadow(remap = false)
    private static void assignToShadow(RenderPipeline pipeline, Function<IrisRenderingPipeline, ShaderKey> o) {
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void add(CallbackInfo ci) {
        IrisPipelines.copyPipeline(RenderPipelines.ENTITY_CUTOUT_CULL, PonderRenderPipelines.ENTITY_BLOCK_SOLID);
        IrisPipelines.copyPipeline(RenderPipelines.ENTITY_CUTOUT_CULL, PonderRenderPipelines.ENTITY_BLOCK_CUTOUT);
        IrisPipelines.copyPipeline(
            RenderPipelines.ENTITY_TRANSLUCENT_CULL,
            PonderRenderPipelines.ENTITY_BLOCK_TRANSLUCENT
        );
        IrisPipelines.copyPipeline(RenderPipelines.SOLID_BLOCK, PonderRenderPipelines.ENTITY_BLOCK_LIGHT_SOLID);
        IrisPipelines.copyPipeline(RenderPipelines.CUTOUT_BLOCK, PonderRenderPipelines.ENTITY_BLOCK_LIGHT_CUTOUT);
        IrisPipelines.copyPipeline(
            RenderPipelines.TRANSLUCENT_BLOCK,
            PonderRenderPipelines.ENTITY_BLOCK_LIGHT_TRANSLUCENT
        );
        IrisPipelines.copyPipeline(RenderPipelines.ENTITY_CUTOUT_CULL, PonderRenderPipelines.NETHER_ENTITY_BLOCK_SOLID);
        IrisPipelines.copyPipeline(
            RenderPipelines.ENTITY_CUTOUT_CULL,
            PonderRenderPipelines.NETHER_ENTITY_BLOCK_CUTOUT
        );
        IrisPipelines.copyPipeline(
            RenderPipelines.ENTITY_TRANSLUCENT_CULL,
            PonderRenderPipelines.NETHER_ENTITY_BLOCK_TRANSLUCENT
        );
        IrisPipelines.copyPipeline(RenderPipelines.SOLID_BLOCK, PonderRenderPipelines.NETHER_ENTITY_BLOCK_LIGHT_SOLID);
        IrisPipelines.copyPipeline(
            RenderPipelines.CUTOUT_BLOCK,
            PonderRenderPipelines.NETHER_ENTITY_BLOCK_LIGHT_CUTOUT
        );
        IrisPipelines.copyPipeline(
            RenderPipelines.TRANSLUCENT_BLOCK,
            PonderRenderPipelines.NETHER_ENTITY_BLOCK_LIGHT_TRANSLUCENT
        );
        IrisPipelines.copyPipeline(
            RenderPipelines.ENTITY_TRANSLUCENT_CULL,
            PonderRenderPipelines.ENTITY_TRANSLUCENT_CULL
        );
        IrisPipelines.copyPipeline(RenderPipelines.ENTITY_TRANSLUCENT, PonderRenderPipelines.ENTITY_TRANSLUCENT);
        IrisPipelines.copyPipeline(RenderPipelines.TRANSLUCENT_PARTICLE, AllRenderPipelines.CUBE);
        IrisPipelines.assignPipeline(AllRenderPipelines.GLOWING, ShaderKey.BLOCK_ENTITY_BRIGHT);
        IrisPipelines.assignPipeline(AllRenderPipelines.GLOWING_TRANSLUCENT, ShaderKey.BE_TRANSLUCENT);
        IrisPipelines.assignPipeline(AllRenderPipelines.ADDITIVE, ShaderKey.BE_TRANSLUCENT);
        assignToShadow(AllRenderPipelines.ADDITIVE, p -> ShaderKey.SHADOW_TRANSLUCENT);
        assignToShadow(AllRenderPipelines.GLOWING, p -> ShaderKey.SHADOW_LIGHTNING);
        assignToShadow(AllRenderPipelines.GLOWING_TRANSLUCENT, p -> ShaderKey.SHADOW_LIGHTNING);
    }
}

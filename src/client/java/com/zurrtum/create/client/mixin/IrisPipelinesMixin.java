package com.zurrtum.create.client.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
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
        IrisPipelines.copyPipeline(RenderPipelines.ITEM_ENTITY_TRANSLUCENT_CULL, PonderRenderPipelines.RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL);
        IrisPipelines.copyPipeline(RenderPipelines.ENTITY_TRANSLUCENT, PonderRenderPipelines.ENTITY_TRANSLUCENT);
        IrisPipelines.copyPipeline(RenderPipelines.SOLID_BLOCK, AllRenderPipelines.ADDITIVE);
        IrisPipelines.copyPipeline(RenderPipelines.SOLID_BLOCK, AllRenderPipelines.ADDITIVE2);
        IrisPipelines.copyPipeline(RenderPipelines.TRANSLUCENT_PARTICLE, AllRenderPipelines.CUBE);
        IrisPipelines.assignPipeline(AllRenderPipelines.GLOWING, ShaderKey.BLOCK_ENTITY_BRIGHT);
        IrisPipelines.assignPipeline(AllRenderPipelines.GLOWING_TRANSLUCENT, ShaderKey.BE_TRANSLUCENT);
        Function<IrisRenderingPipeline, ShaderKey> getter = (p) -> ShaderKey.SHADOW_ENTITIES_CUTOUT;
        assignToShadow(AllRenderPipelines.GLOWING, getter);
        assignToShadow(AllRenderPipelines.GLOWING_TRANSLUCENT, getter);
    }
}

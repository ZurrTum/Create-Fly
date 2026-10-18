package com.zurrtum.create.client.mixin;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.zurrtum.create.client.foundation.render.CustomRenderPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RenderPipeline.class)
public class RenderPipelineMixin implements CustomRenderPipeline {
    @Unique
    private boolean solidBlend;

    @Override
    public boolean create$isSolidBlend() {
        return solidBlend;
    }

    @Override
    public void create$markSolidBlend() {
        solidBlend = true;
    }
}

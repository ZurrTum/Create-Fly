package com.zurrtum.create.client.foundation.render;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;

public interface CustomRenderPipeline {
    boolean create$isSolidBlend();

    void create$markSolidBlend();

    static RenderPipeline markSolidBlend(RenderPipeline pipeline) {
        ((CustomRenderPipeline) pipeline).create$markSolidBlend();
        return pipeline;
    }
}

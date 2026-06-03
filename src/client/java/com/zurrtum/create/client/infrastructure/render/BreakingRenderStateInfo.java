package com.zurrtum.create.client.infrastructure.render;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import org.jspecify.annotations.Nullable;

public interface BreakingRenderStateInfo {
    void create$setRenderModel(@Nullable BlockStateModel model);

    @Nullable BlockStateModel create$getRenderModel();
}

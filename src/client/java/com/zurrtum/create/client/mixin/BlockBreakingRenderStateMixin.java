package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.infrastructure.render.BreakingRenderStateInfo;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockBreakingRenderState.class)
public class BlockBreakingRenderStateMixin implements BreakingRenderStateInfo {
    @Unique
    private BlockStateModel model;

    @Override
    public void create$setRenderModel(@Nullable BlockStateModel model) {
        this.model = model;
    }

    @Override
    public BlockStateModel create$getRenderModel() {
        return model;
    }
}

package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.compat.fabric.WrapperModel;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WrapperBlockStateModel.class)
public class WrapperBlockStateModelMixin implements WrapperModel {
    @Shadow
    protected BlockStateModel wrapped;

    @Override
    public BlockStateModel create$getWrapped() {
        return wrapped;
    }
}

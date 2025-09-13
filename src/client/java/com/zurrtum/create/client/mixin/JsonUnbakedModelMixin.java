package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.model.LayerUnbakedModel;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(JsonUnbakedModel.class)
public class JsonUnbakedModelMixin implements LayerUnbakedModel {
    @Unique
    private BlockRenderLayer blockRenderLayer;

    @Override
    @Nullable
    public BlockRenderLayer create$getBlockRenderLayer() {
        return blockRenderLayer;
    }

    @Override
    public void create$setBlockRenderLayer(BlockRenderLayer blockRenderLayer) {
        this.blockRenderLayer = blockRenderLayer;
    }
}

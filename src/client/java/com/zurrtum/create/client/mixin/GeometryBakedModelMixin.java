package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.model.LayerBakedModel;
import com.zurrtum.create.client.model.LayerUnbakedModel;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.model.BakedSimpleModel;
import net.minecraft.client.render.model.GeometryBakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GeometryBakedModel.class)
public class GeometryBakedModelMixin implements LayerBakedModel {
    @Unique
    private BlockRenderLayer blockRenderLayer;

    @Override
    public BlockRenderLayer create$getBlockRenderLayer() {
        return blockRenderLayer;
    }

    @Override
    public void create$setBlockRenderLayer(BlockRenderLayer blockRenderLayer) {
        this.blockRenderLayer = blockRenderLayer;
    }

    @ModifyReturnValue(method = "create(Lnet/minecraft/client/render/model/Baker;Lnet/minecraft/util/Identifier;Lnet/minecraft/client/render/model/ModelBakeSettings;)Lnet/minecraft/client/render/model/GeometryBakedModel;", at = @At("RETURN"))
    private static GeometryBakedModel addBlockRenderLayer(GeometryBakedModel geometry, @Local BakedSimpleModel model) {
        return LayerUnbakedModel.setBlockRenderLayer(geometry, model);
    }
}

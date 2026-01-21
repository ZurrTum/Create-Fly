package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.model.LayerBakedModel;
import com.zurrtum.create.client.model.LayerUnbakedModel;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.ResolvedModel;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SimpleModelWrapper.class)
public class SimpleModelWrapperMixin implements LayerBakedModel {
    @Unique
    private ChunkSectionLayer blockRenderLayer;

    @Override
    public ChunkSectionLayer create$getBlockRenderLayer() {
        return blockRenderLayer;
    }

    @Override
    public void create$setBlockRenderLayer(@NonNull ChunkSectionLayer blockRenderLayer) {
        this.blockRenderLayer = blockRenderLayer;
    }

    @ModifyReturnValue(method = "bake(Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/resources/Identifier;Lnet/minecraft/client/resources/model/ModelState;)Lnet/minecraft/client/renderer/block/model/BlockModelPart;", at = @At("RETURN"))
    private static BlockModelPart addBlockRenderLayer(BlockModelPart part, @Local ResolvedModel model) {
        return LayerUnbakedModel.setBlockRenderLayer(part, model);
    }
}

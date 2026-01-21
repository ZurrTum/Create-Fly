package com.zurrtum.create.client.mixin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.client.model.LayerUnbakedModel;
import com.zurrtum.create.client.model.UnbakedModelParser;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockModel.class)
public class BlockModelMixin implements LayerUnbakedModel {
    @Unique
    private ChunkSectionLayer blockRenderLayer;

    @Override
    @Nullable
    public ChunkSectionLayer create$getBlockRenderLayer() {
        return blockRenderLayer;
    }

    @Override
    public void create$setBlockRenderLayer(@NonNull ChunkSectionLayer blockRenderLayer) {
        this.blockRenderLayer = blockRenderLayer;
    }

    @WrapOperation(method = "<clinit>()V", at = @At(value = "INVOKE", target = "Lcom/google/gson/GsonBuilder;create()Lcom/google/gson/Gson;"))
    private static Gson wrap(GsonBuilder instance, Operation<Gson> original) {
        return UnbakedModelParser.wrap(original.call(instance));
    }
}

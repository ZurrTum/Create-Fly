package com.zurrtum.create.client.mixin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.client.model.LayerUnbakedModel;
import com.zurrtum.create.client.model.UnbakedModelParser;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

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

    @WrapOperation(method = "<clinit>()V", at = @At(value = "INVOKE", target = "Lcom/google/gson/GsonBuilder;create()Lcom/google/gson/Gson;"))
    private static Gson wrap(GsonBuilder instance, Operation<Gson> original) {
        return UnbakedModelParser.wrap(original.call(instance));
    }
}

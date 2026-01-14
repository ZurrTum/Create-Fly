package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModelEventHandler;
import net.minecraft.client.render.model.GeometryBakedModel;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.util.thread.AsyncHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(value = ModelBaker.class, priority = 999)
public class ModelBakerMixin {
    @ModifyReturnValue(method = "bake(Lnet/minecraft/client/render/model/ErrorCollectingSpriteGetter;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;", at = @At("RETURN"))
    private CompletableFuture<ModelBaker.BakedModels> bakeModels(
        CompletableFuture<ModelBaker.BakedModels> bakedModelFuture,
        @Local(argsOnly = true) Executor taskExecutor,
        @Local ModelBaker.BakerImpl baker
    ) {
        return AsyncHelper.mapValues(
            PartialModelEventHandler.getRegisterAdditional(), (id, model) -> {
                GeometryBakedModel bakedModel = GeometryBakedModel.create(baker, id, ModelRotation.X0_Y0);
                PartialModelEventHandler.onBakingCompleted(model, bakedModel);
                return bakedModel;
            }, taskExecutor
        ).thenAccept(PartialModelEventHandler::onBakingCompleted).thenCompose(v -> bakedModelFuture);
    }
}

package com.zurrtum.create.client.mixin;

import com.google.common.cache.LoadingCache;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.render.model.BakedSimpleModel;
import net.minecraft.client.render.model.ErrorCollectingSpriteGetter;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.render.model.ModelBaker.BakerImpl;
import net.minecraft.client.render.model.ReferencedModelsCollector;
import net.minecraft.util.Identifier;
import org.embeddedt.modernfix.dynamicresources.DynamicModelProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(value = ModelBaker.class, priority = 998)
public class DynamicBakerImplMixin {
    @Inject(method = "bake(Lnet/minecraft/client/render/model/ErrorCollectingSpriteGetter;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;", at = @At("RETURN"))
    private void updateDynamicBacker(
        ErrorCollectingSpriteGetter spriteGetter,
        Executor executor,
        CallbackInfoReturnable<CompletableFuture<ModelBaker.BakedModels>> cir,
        @Local LocalRef<BakerImpl> bakerImpl
    ) {
        DynamicModelProviderAccessor accessor = (DynamicModelProviderAccessor) DynamicModelProvider.currentReloadingModelProvider.get();
        if (accessor == null) {
            return;
        }
        ReferencedModelsCollector.Holder resolvedMissingModel = accessor.getResolvedMissingModel();
        LoadingCache<Identifier, Optional<ReferencedModelsCollector.Holder>> resolvedBlockModels = accessor.getResolvedBlockModels();
        bakerImpl.set(((ModelBaker) (Object) this).new BakerImpl(accessor.getTextureGetter()) {
            @Override
            public BakedSimpleModel getModel(Identifier id) {
                return resolvedBlockModels.getUnchecked(id).orElse(resolvedMissingModel);
            }
        });
    }
}

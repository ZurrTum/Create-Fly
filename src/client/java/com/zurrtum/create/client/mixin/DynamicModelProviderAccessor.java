package com.zurrtum.create.client.mixin;

import com.google.common.cache.LoadingCache;
import net.minecraft.client.render.model.ErrorCollectingSpriteGetter;
import net.minecraft.client.render.model.ReferencedModelsCollector;
import net.minecraft.util.Identifier;
import org.embeddedt.modernfix.dynamicresources.DynamicModelProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(DynamicModelProvider.class)
public interface DynamicModelProviderAccessor {
    @Accessor(value = "resolvedBlockModels", remap = false)
    LoadingCache<Identifier, Optional<ReferencedModelsCollector.Holder>> getResolvedBlockModels();

    @Accessor(value = "resolvedMissingModel", remap = false)
    ReferencedModelsCollector.Holder getResolvedMissingModel();

    @Accessor(value = "textureGetter", remap = false)
    ErrorCollectingSpriteGetter getTextureGetter();
}

package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.catnip.render.PonderRenderTypes;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.BufferAllocator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BufferBuilderStorage.class)
public class BufferBuilderStorageMixin {
    @Inject(method = "method_54639(Lit/unimi/dsi/fastutil/objects/Object2ObjectLinkedOpenHashMap;)V", at = @At("TAIL"))
    private void registerLayers(Object2ObjectLinkedOpenHashMap<RenderLayer, BufferAllocator> map, CallbackInfo ci) {
        map.put(PonderRenderTypes.translucent(), new BufferAllocator(PonderRenderTypes.translucent().getExpectedBufferSize()));
        map.put(PonderRenderTypes.fluid(), new BufferAllocator(PonderRenderTypes.fluid().getExpectedBufferSize()));
    }
}

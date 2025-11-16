package com.zurrtum.create.client.mixin;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.zurrtum.create.client.foundation.render.CreateRenderTypes;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderBuffers.class)
public abstract class RenderBuffersMixin {
    @Shadow
    private static void put(Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> builderStorage, RenderType layer) {
    }

    @Inject(method = "lambda$new$0(Lit/unimi/dsi/fastutil/objects/Object2ObjectLinkedOpenHashMap;)V", at = @At("TAIL"))
    private void registerLayers(Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> map, CallbackInfo ci) {
        put(map, CreateRenderTypes.additive2());
        put(map, CreateRenderTypes.translucent());
        put(map, CreateRenderTypes.additive());
    }
}

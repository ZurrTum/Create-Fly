package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.catnip.render.StitchedSprite;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureAtlas.class)
public class TextureAtlasMixin {
    @Inject(method = "upload(Lnet/minecraft/client/renderer/texture/SpriteLoader$Preparations;)V", at = @At("TAIL"))
    private void onTextureStitchPost(SpriteLoader.Preparations stitchResult, CallbackInfo ci) {
        StitchedSprite.onTextureStitchPost((TextureAtlas) (Object) this);
    }
}

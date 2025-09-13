package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.catnip.render.StitchedSprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.SpriteLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteAtlasTexture.class)
public class SpriteAtlasTextureMixin {
    @Inject(method = "upload(Lnet/minecraft/client/texture/SpriteLoader$StitchResult;)V", at = @At("TAIL"))
    private void onTextureStitchPost(SpriteLoader.StitchResult stitchResult, CallbackInfo ci) {
        StitchedSprite.onTextureStitchPost((SpriteAtlasTexture) (Object) this);
    }
}

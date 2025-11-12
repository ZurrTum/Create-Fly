package com.zurrtum.create.mixin;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.foundation.recipe.IngredientTextContent;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ComponentSerialization.class)
public class TextCodecsMixin {
    @Inject(method = "bootstrap(Lnet/minecraft/util/ExtraCodecs$LateBoundIdMapper;)V", at = @At("TAIL"))
    private static void registerTypes(ExtraCodecs.LateBoundIdMapper<String, MapCodec<? extends ComponentContents>> idMapper, CallbackInfo ci) {
        idMapper.put("ingredient", IngredientTextContent.CODEC);
    }
}

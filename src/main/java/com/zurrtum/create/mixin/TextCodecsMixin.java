package com.zurrtum.create.mixin;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.foundation.recipe.IngredientTextContent;
import net.minecraft.text.TextCodecs;
import net.minecraft.text.TextContent;
import net.minecraft.util.dynamic.Codecs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextCodecs.class)
public class TextCodecsMixin {
    @Inject(method = "registerTypes(Lnet/minecraft/util/dynamic/Codecs$IdMapper;)V", at = @At("TAIL"))
    private static void registerTypes(Codecs.IdMapper<String, MapCodec<? extends TextContent>> idMapper, CallbackInfo ci) {
        idMapper.put("ingredient", IngredientTextContent.CODEC);
    }
}

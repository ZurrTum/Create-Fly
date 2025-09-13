package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.foundation.recipe.IngredientTextContent;
import net.minecraft.text.TextCodecs;
import net.minecraft.text.TextContent;
import net.minecraft.util.StringIdentifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mixin(TextCodecs.class)
public class TextCodecsMixin {
    @WrapOperation(method = "createCodec(Lcom/mojang/serialization/Codec;)Lcom/mojang/serialization/Codec;", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/TextCodecs;dispatchingCodec([Lnet/minecraft/util/StringIdentifiable;Ljava/util/function/Function;Ljava/util/function/Function;Ljava/lang/String;)Lcom/mojang/serialization/MapCodec;"))
    private static MapCodec<TextContent> addType(
        StringIdentifiable[] types,
        Function<StringIdentifiable, MapCodec<TextContent>> typeToCodec,
        Function<TextContent, StringIdentifiable> valueToType,
        String dispatchingKey,
        Operation<MapCodec<TextContent>> original
    ) {
        ArrayList<StringIdentifiable> list = new ArrayList<>(List.of(types));
        list.add(IngredientTextContent.TYPE);
        return original.call(list.toArray(StringIdentifiable[]::new), typeToCodec, valueToType, dispatchingKey);
    }
}

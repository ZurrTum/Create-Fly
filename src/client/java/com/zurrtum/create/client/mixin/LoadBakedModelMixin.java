package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.Reader;

@Mixin(BakedModelManager.class)
public class LoadBakedModelMixin {
    @Inject(method = "method_65750", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/json/JsonUnbakedModel;deserialize(Ljava/io/Reader;)Lnet/minecraft/client/render/model/json/JsonUnbakedModel;"), cancellable = true)
    private static void deserialize(CallbackInfoReturnable<Pair<Identifier, UnbakedModel>> cir, @Local Identifier identifier, @Local Reader input) {
        try {
            UnbakedModel model = JsonHelper.deserialize(JsonUnbakedModel.GSON, input, UnbakedModel.class);
            cir.setReturnValue(Pair.of(identifier, model));
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception ignore) {
                }
            }
        }
    }
}

package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.embeddedt.modernfix.dynamicresources.DynamicModelProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.Reader;
import java.util.Optional;

@Mixin(DynamicModelProvider.class)
public class DynamicModelProviderMixin {
    @Inject(method = "loadBlockModelDefault(Lnet/minecraft/util/Identifier;)Ljava/util/Optional;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/json/JsonUnbakedModel;deserialize(Ljava/io/Reader;)Lnet/minecraft/client/render/model/json/JsonUnbakedModel;"), cancellable = true)
    private void deserialize(Identifier identifier, CallbackInfoReturnable<Optional<UnbakedModel>> cir, @Local Reader input) {
        try {
            cir.setReturnValue(Optional.of(JsonHelper.deserialize(JsonUnbakedModel.GSON, input, UnbakedModel.class)));
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

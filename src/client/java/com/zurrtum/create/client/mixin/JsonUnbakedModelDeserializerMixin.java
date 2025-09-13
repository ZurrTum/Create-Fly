package com.zurrtum.create.client.mixin;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.model.LayerUnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(JsonUnbakedModel.Deserializer.class)
public class JsonUnbakedModelDeserializerMixin {
    @ModifyReturnValue(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/render/model/json/JsonUnbakedModel;", at = @At("RETURN"))
    private JsonUnbakedModel checkLayer(JsonUnbakedModel model, @Local JsonObject jsonObject) {
        return LayerUnbakedModel.setBlockRenderLayer(model, jsonObject);
    }
}

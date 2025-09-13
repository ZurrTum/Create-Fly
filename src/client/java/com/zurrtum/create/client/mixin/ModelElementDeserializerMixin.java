package com.zurrtum.create.client.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.model.NormalsModelElement;
import net.minecraft.client.render.model.json.ModelElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModelElement.Deserializer.class)
public class ModelElementDeserializerMixin {
    @ModifyReturnValue(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/render/model/json/ModelElement;", at = @At("RETURN"))
    private ModelElement checkNormals(ModelElement element, @Local JsonObject jsonObject) {
        JsonElement data = jsonObject.get("neoforge_data");
        if (data != null) {
            try {
                JsonElement value = data.getAsJsonObject().get("calculate_normals");
                if (value != null && value.getAsBoolean()) {
                    NormalsModelElement.markNormals(element);
                }
            } catch (Exception ignored) {
            }
        }
        return element;
    }
}

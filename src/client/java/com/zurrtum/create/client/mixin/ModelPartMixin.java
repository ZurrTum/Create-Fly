package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.flywheel.lib.model.baked.DualVertexConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.ItemMeshEmitter;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.SpriteTexturedVertexConsumer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPart.class)
public class ModelPartMixin {
    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V", at = @At("HEAD"), cancellable = true)
    private void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color, CallbackInfo ci) {
        if (vertices instanceof SpriteTexturedVertexConsumer consumer) {
            VertexConsumer delegate = consumer.delegate;
            if (delegate instanceof ItemMeshEmitter emitter) {
                emitter.emit((ModelPart) (Object) this, matrices, consumer.sprite, null, light, overlay, color);
                ci.cancel();
            } else if (delegate instanceof DualVertexConsumer dual) {
                dual.emit((ModelPart) (Object) this, matrices, consumer.sprite, light, overlay, color);
                ci.cancel();
            }
        }
    }
}

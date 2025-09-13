package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.flywheel.lib.model.baked.DualVertexConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.ItemMeshEmitter;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.SpriteTexturedVertexConsumer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Model.class)
public abstract class ModelMixin {
    @Shadow
    public abstract ModelPart getRootPart();

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V", at = @At("HEAD"), cancellable = true)
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color, CallbackInfo ci) {
        if (vertices instanceof ItemMeshEmitter emitter) {
            emitter.emit(getRootPart(), matrices, null, null, light, overlay, color);
            ci.cancel();
        } else if (vertices instanceof SpriteTexturedVertexConsumer consumer) {
            if (consumer.delegate instanceof ItemMeshEmitter emitter) {
                emitter.emit(getRootPart(), matrices, consumer.sprite, null, light, overlay, color);
                ci.cancel();
            }
        } else if (vertices instanceof DualVertexConsumer dual) {
            dual.emit(getRootPart(), matrices, null, light, overlay, color);
            ci.cancel();
        }
    }
}

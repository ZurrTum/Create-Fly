package com.zurrtum.create.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.DualVertexConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.ItemMeshEmitter;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SpriteCoordinateExpander;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Model.class)
public abstract class ModelMixin {
    @Shadow
    public abstract ModelPart root();

    @Inject(method = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", at = @At("HEAD"), cancellable = true)
    public void render(PoseStack matrices, VertexConsumer vertices, int light, int overlay, int color, CallbackInfo ci) {
        if (vertices instanceof ItemMeshEmitter emitter) {
            emitter.emit(root(), matrices, null, null, light, overlay, color);
            ci.cancel();
        } else if (vertices instanceof SpriteCoordinateExpander consumer) {
            if (consumer.delegate instanceof ItemMeshEmitter emitter) {
                emitter.emit(root(), matrices, consumer.sprite, null, light, overlay, color);
                ci.cancel();
            }
        } else if (vertices instanceof DualVertexConsumer dual) {
            dual.emit(root(), matrices, null, light, overlay, color);
            ci.cancel();
        }
    }
}

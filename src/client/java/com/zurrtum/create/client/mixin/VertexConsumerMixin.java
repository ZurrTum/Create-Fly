package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.model.NormalsBakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(VertexConsumer.class)
public interface VertexConsumerMixin {
    @ModifyVariable(method = "putBlockBakedQuad(FFFLnet/minecraft/client/resources/model/geometry/BakedQuad;Lcom/mojang/blaze3d/vertex/QuadInstance;)V", at = @At(value = "STORE", ordinal = 0))
    private Vector3fc getNormal(Vector3fc normal, @Local(argsOnly = true) BakedQuad quad) {
        return ((NormalsBakedQuad) (Object) quad).create$getNormal(normal);
    }

    @ModifyVariable(method = "putBakedQuad(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/resources/model/geometry/BakedQuad;Lcom/mojang/blaze3d/vertex/QuadInstance;)V", at = @At(value = "STORE", ordinal = 0))
    private Vector3fc getNormalVec(Vector3fc normalVec, @Local(argsOnly = true) BakedQuad quad) {
        return ((NormalsBakedQuad) (Object) quad).create$getNormal(normalVec);
    }
}

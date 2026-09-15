package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.model.NormalsBakedQuad;
import com.zurrtum.create.client.model.NormalsModelElement;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.cuboid.UnbakedCuboidGeometry;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import org.joml.GeometryUtils;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(UnbakedCuboidGeometry.class)
public class UnbakedCuboidGeometryMixin {
    @Inject(method = "bake(Ljava/util/List;Lnet/minecraft/client/resources/model/sprite/TextureSlots;Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/client/renderer/block/dispatch/ModelState;Lnet/minecraft/client/resources/model/ModelDebugName;)Lnet/minecraft/client/resources/model/geometry/QuadCollection;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/cuboid/CuboidFace;cullForDirection()Lnet/minecraft/core/Direction;", ordinal = 0))
    private static void calcNormal(
        CallbackInfoReturnable<QuadCollection> cir,
        @Local BakedQuad quad,
        @Local CuboidModelElement element
    ) {
        if (((NormalsModelElement) (Object) element).create$calcNormals()) {
            Vector3f normal = new Vector3f();
            GeometryUtils.normal(quad.position0(), quad.position1(), quad.position2(), normal);
            ((NormalsBakedQuad) (Object) quad).create$setNormal(normal);
        }
    }
}

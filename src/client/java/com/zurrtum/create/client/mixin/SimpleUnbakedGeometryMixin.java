package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.model.NormalsBakedQuad;
import com.zurrtum.create.client.model.NormalsModelElement;
import com.zurrtum.create.client.model.NormalsModelElement.NormalsType;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SimpleUnbakedGeometry.class)
public class SimpleUnbakedGeometryMixin {
    @WrapOperation(method = "bake(Ljava/util/List;Lnet/minecraft/client/renderer/block/model/TextureSlots;Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/client/resources/model/ModelState;Lnet/minecraft/client/resources/model/ModelDebugName;)Lnet/minecraft/client/resources/model/QuadCollection;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/model/FaceBakery;bakeQuad(Lnet/minecraft/client/resources/model/ModelBaker$PartCache;Lorg/joml/Vector3fc;Lorg/joml/Vector3fc;Lnet/minecraft/client/renderer/block/model/BlockElementFace;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/core/Direction;Lnet/minecraft/client/resources/model/ModelState;Lnet/minecraft/client/renderer/block/model/BlockElementRotation;ZI)Lnet/minecraft/client/renderer/block/model/BakedQuad;"))
    private static BakedQuad bakeQuad(
        final ModelBaker.PartCache partCache,
        final Vector3fc from,
        final Vector3fc to,
        final BlockElementFace face,
        final TextureAtlasSprite icon,
        final Direction facing,
        final ModelState modelState,
        @Nullable final BlockElementRotation elementRotation,
        final boolean shade,
        final int lightEmission,
        Operation<BakedQuad> original,
        @Local BlockElement element
    ) {
        BakedQuad quad = original.call(partCache, from, to, face, icon, facing, modelState, elementRotation, shade, lightEmission);
        NormalsType type = NormalsModelElement.getNormalsType(element);
        if (type != null) {
            Vector3fc vector;
            if (type == NormalsType.CALC) {
                Vector3f v1 = new Vector3f(quad.position3());
                Vector3fc t1 = quad.position1();
                Vector3f v2 = new Vector3f(quad.position2());
                Vector3fc t2 = quad.position0();
                v1.sub(t1);
                v2.sub(t2);
                v2.cross(v1);
                vector = v2.normalize();
            } else {
                vector = quad.direction().getUnitVec3f();
            }

            int x = ((byte) Math.round(vector.x() * 127)) & 0xFF;
            int y = ((byte) Math.round(vector.y() * 127)) & 0xFF;
            int z = ((byte) Math.round(vector.z() * 127)) & 0xFF;
            int normal = x | (y << 0x08) | (z << 0x10);
            NormalsBakedQuad.setNormals(quad, new int[]{normal, normal, normal, normal});
        }
        return quad;
    }
}

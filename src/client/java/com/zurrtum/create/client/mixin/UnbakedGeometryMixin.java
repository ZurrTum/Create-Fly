package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.model.NormalsBakedQuad;
import com.zurrtum.create.client.model.NormalsModelElement;
import com.zurrtum.create.client.model.NormalsModelElement.NormalsType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.SimpleUnbakedGeometry;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SimpleUnbakedGeometry.class)
public class UnbakedGeometryMixin {
    @ModifyReturnValue(method = "bakeFace(Lnet/minecraft/client/renderer/block/model/BlockElement;Lnet/minecraft/client/renderer/block/model/BlockElementFace;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/core/Direction;Lnet/minecraft/client/resources/model/ModelState;)Lnet/minecraft/client/renderer/block/model/BakedQuad;", at = @At("RETURN"))
    private static BakedQuad bakeQuad(BakedQuad quad, @Local(argsOnly = true) BlockElement element) {
        NormalsType type = NormalsModelElement.getNormalsType(element);
        if (type != null) {
            int[] faceData = quad.vertices();
            Vector3fc vector;
            if (type == NormalsType.CALC) {
                Vector3f v1 = getVertexPos(faceData, 3);
                Vector3f t1 = getVertexPos(faceData, 1);
                Vector3f v2 = getVertexPos(faceData, 2);
                Vector3f t2 = getVertexPos(faceData, 0);
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

            for (int i = 0; i < 4; i++) {
                faceData[i * 8 + 7] = normal;
            }
            NormalsBakedQuad.markNormals(quad);
        }
        return quad;
    }

    @Unique
    private static Vector3f getVertexPos(int[] data, int vertex) {
        int idx = vertex * 8;

        float x = Float.intBitsToFloat(data[idx]);
        float y = Float.intBitsToFloat(data[idx + 1]);
        float z = Float.intBitsToFloat(data[idx + 2]);

        return new Vector3f(x, y, z);
    }
}

package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.model.NormalsBakedQuad;
import com.zurrtum.create.client.model.NormalsModelElement;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.UnbakedGeometry;
import net.minecraft.client.render.model.json.ModelElement;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(UnbakedGeometry.class)
public class UnbakedGeometryMixin {
    @ModifyReturnValue(method = "bakeQuad(Lnet/minecraft/client/render/model/json/ModelElement;Lnet/minecraft/client/render/model/json/ModelElementFace;Lnet/minecraft/client/texture/Sprite;Lnet/minecraft/util/math/Direction;Lnet/minecraft/client/render/model/ModelBakeSettings;)Lnet/minecraft/client/render/model/BakedQuad;", at = @At("RETURN"))
    private static BakedQuad bakeQuad(BakedQuad quad, @Local(argsOnly = true) ModelElement element) {
        if (NormalsModelElement.calcNormals(element)) {
            int[] faceData = quad.vertexData();
            Vector3f v1 = getVertexPos(faceData, 3);
            Vector3f t1 = getVertexPos(faceData, 1);
            Vector3f v2 = getVertexPos(faceData, 2);
            Vector3f t2 = getVertexPos(faceData, 0);
            v1.sub(t1);
            v2.sub(t2);
            v2.cross(v1);
            Vector3fc vector = v2.normalize();

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

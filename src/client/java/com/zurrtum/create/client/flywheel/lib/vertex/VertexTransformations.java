package com.zurrtum.create.client.flywheel.lib.vertex;

import com.zurrtum.create.client.flywheel.api.vertex.MutableVertexList;
import net.minecraft.client.texture.Sprite;

public final class VertexTransformations {
    private VertexTransformations() {
    }

    public static void retexture(MutableVertexList vertexList, int index, Sprite sprite) {
        vertexList.u(index, sprite.getFrameU(vertexList.u(index)));
        vertexList.v(index, sprite.getFrameV(vertexList.v(index)));
    }

    public static void retexture(MutableVertexList vertexList, Sprite sprite) {
        for (int i = 0; i < vertexList.vertexCount(); i++) {
            retexture(vertexList, i, sprite);
        }
    }
}

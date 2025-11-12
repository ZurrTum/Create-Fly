package com.zurrtum.create.client.flywheel.lib.vertex;

import com.zurrtum.create.client.flywheel.api.vertex.MutableVertexList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public final class VertexTransformations {
    private VertexTransformations() {
    }

    public static void retexture(MutableVertexList vertexList, int index, TextureAtlasSprite sprite) {
        vertexList.u(index, sprite.getU(vertexList.u(index)));
        vertexList.v(index, sprite.getV(vertexList.v(index)));
    }

    public static void retexture(MutableVertexList vertexList, TextureAtlasSprite sprite) {
        for (int i = 0; i < vertexList.vertexCount(); i++) {
            retexture(vertexList, i, sprite);
        }
    }
}

package com.zurrtum.create.client.model;

import net.minecraft.client.renderer.block.model.BakedQuad;

public interface NormalsBakedQuad {
    void create$setNormals(int[] normal);

    int[] create$getNormals();

    static int[] getNormals(BakedQuad quad) {
        return ((NormalsBakedQuad) (Object) quad).create$getNormals();
    }

    static void setNormals(BakedQuad quad, int[] normals) {
        ((NormalsBakedQuad) (Object) quad).create$setNormals(normals);
    }
}

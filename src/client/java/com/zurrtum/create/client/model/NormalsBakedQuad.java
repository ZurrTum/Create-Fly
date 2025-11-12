package com.zurrtum.create.client.model;

import net.minecraft.client.renderer.block.model.BakedQuad;

public interface NormalsBakedQuad {
    void create$markNormals();

    boolean create$hasNormals();

    static boolean hasNormals(BakedQuad quad) {
        return ((NormalsBakedQuad) (Object) quad).create$hasNormals();
    }

    static void markNormals(BakedQuad quad) {
        ((NormalsBakedQuad) (Object) quad).create$markNormals();
    }
}

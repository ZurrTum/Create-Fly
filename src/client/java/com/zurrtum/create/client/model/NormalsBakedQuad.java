package com.zurrtum.create.client.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import org.jspecify.annotations.Nullable;

public interface NormalsBakedQuad {
    void create$setNormals(int @Nullable [] normal);

    int @Nullable [] create$getNormals();

    static int @Nullable [] getNormals(BakedQuad quad) {
        return ((NormalsBakedQuad) (Object) quad).create$getNormals();
    }

    static void setNormals(BakedQuad quad, int @Nullable [] normals) {
        ((NormalsBakedQuad) (Object) quad).create$setNormals(normals);
    }
}

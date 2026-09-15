package com.zurrtum.create.client.model;

import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public interface NormalsBakedQuad {
    void create$setNormal(@Nullable Vector3fc normal);

    @Nullable Vector3fc create$getNormal();

    Vector3fc create$getNormal(Vector3fc def);
}

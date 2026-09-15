package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.model.NormalsBakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BakedQuad.class)
public class BakedQuadMixin implements NormalsBakedQuad {
    @Unique
    @Nullable
    private Vector3fc normal;

    @Override
    public void create$setNormal(Vector3fc normal) {
        this.normal = normal;
    }

    @Override
    @Nullable
    public Vector3fc create$getNormal() {
        return normal;
    }

    @Override
    @NonNull
    public Vector3fc create$getNormal(@NonNull Vector3fc def) {
        return normal == null ? def : normal;
    }
}

package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.model.NormalsBakedQuad;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BakedQuad.class)
public class BakedQuadMixin implements NormalsBakedQuad {
    @Unique
    private int @Nullable [] normals;

    @Override
    public void create$setNormals(int @Nullable [] normals) {
        this.normals = normals;
    }

    @Override
    public int @Nullable [] create$getNormals() {
        return normals;
    }
}

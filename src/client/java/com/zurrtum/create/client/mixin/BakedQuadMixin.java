package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.model.NormalsBakedQuad;
import net.minecraft.client.render.model.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BakedQuad.class)
public class BakedQuadMixin implements NormalsBakedQuad {
    @Unique
    private boolean normals;

    @Override
    public void create$markNormals() {
        normals = true;
    }

    @Override
    public boolean create$hasNormals() {
        return normals;
    }
}

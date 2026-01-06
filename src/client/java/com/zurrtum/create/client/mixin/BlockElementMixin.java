package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.model.NormalsModelElement;
import net.minecraft.client.renderer.block.model.BlockElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockElement.class)
public class BlockElementMixin implements NormalsModelElement {
    @Unique
    private boolean normals;

    @Override
    public boolean create$calcNormals() {
        return normals;
    }

    @Override
    public void create$markNormals() {
        normals = true;
    }
}

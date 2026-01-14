package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.model.NormalsModelElement;
import net.minecraft.client.render.model.json.ModelElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ModelElement.class)
public class ModelElementMixin implements NormalsModelElement {
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

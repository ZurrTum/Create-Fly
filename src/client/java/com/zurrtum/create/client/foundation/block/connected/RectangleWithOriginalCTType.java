package com.zurrtum.create.client.foundation.block.connected;

import net.minecraft.resources.Identifier;

import static com.zurrtum.create.Create.MOD_ID;
import static com.zurrtum.create.client.foundation.block.connected.RectangleCTType.MAP;
import static com.zurrtum.create.client.foundation.block.connected.RectangleCTType.SIZE;

public class RectangleWithOriginalCTType extends CTType {
    public RectangleWithOriginalCTType() {
        super(Identifier.fromNamespaceAndPath(MOD_ID, "rectangle_with_original"), SIZE, AXIS_ALIGNED);
    }

    @Override
    public int getTextureIndex(int context) {
        return MAP[context & AXIS_ALIGNED_FLAGS];
    }

    @Override
    public boolean replaceOriginal() {
        return true;
    }
}

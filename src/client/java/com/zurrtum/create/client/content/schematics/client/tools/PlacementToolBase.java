package com.zurrtum.create.client.content.schematics.client.tools;

import net.minecraft.client.Minecraft;

public abstract class PlacementToolBase extends SchematicToolBase {
    @Override
    public boolean handleMouseWheel(double delta) {
        return false;
    }

    @Override
    public boolean handleRightClick(Minecraft mc) {
        return false;
    }
}

package com.zurrtum.create.client.content.schematics.client.tools;

import net.minecraft.client.MinecraftClient;

public abstract class PlacementToolBase extends SchematicToolBase {
    @Override
    public boolean handleMouseWheel(double delta) {
        return false;
    }

    @Override
    public boolean handleRightClick(MinecraftClient mc) {
        return false;
    }
}

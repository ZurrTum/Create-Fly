package com.zurrtum.create.client.content.schematics.client.tools;

import net.minecraft.client.MinecraftClient;

public class PlaceTool extends SchematicToolBase {

    @Override
    public boolean handleRightClick(MinecraftClient mc) {
        schematicHandler.printInstantly(mc);
        return true;
    }

    @Override
    public boolean handleMouseWheel(double delta) {
        return false;
    }

}

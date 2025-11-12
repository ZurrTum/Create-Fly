package com.zurrtum.create.client.content.schematics.client.tools;

import net.minecraft.client.Minecraft;

public class PlaceTool extends SchematicToolBase {

    @Override
    public boolean handleRightClick(Minecraft mc) {
        schematicHandler.printInstantly(mc);
        return true;
    }

    @Override
    public boolean handleMouseWheel(double delta) {
        return false;
    }

}

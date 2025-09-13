package com.zurrtum.create.client.content.schematics.client.tools;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.content.schematics.client.SchematicTransformation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;

public class MoveTool extends PlacementToolBase {

    @Override
    public void init() {
        super.init();
        renderSelectedFace = true;
    }

    @Override
    public void updateSelection(MinecraftClient mc) {
        super.updateSelection(mc);
    }

    @Override
    public boolean handleMouseWheel(double delta) {
        if (!schematicSelected || !selectedFace.getAxis().isHorizontal())
            return true;

        SchematicTransformation transformation = schematicHandler.getTransformation();
        Vec3d vec = Vec3d.of(selectedFace.getVector()).multiply(-Math.signum(delta));
        vec = vec.multiply(transformation.getMirrorModifier(Axis.X), 1, transformation.getMirrorModifier(Axis.Z));
        vec = VecHelper.rotate(vec, transformation.getRotationTarget(), Axis.Y);
        transformation.move((int) vec.x, 0, (int) vec.z);
        schematicHandler.markDirty();

        return true;
    }

}

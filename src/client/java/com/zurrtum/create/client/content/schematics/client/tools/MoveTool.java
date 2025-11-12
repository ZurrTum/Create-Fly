package com.zurrtum.create.client.content.schematics.client.tools;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.content.schematics.client.SchematicTransformation;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.Vec3;

public class MoveTool extends PlacementToolBase {

    @Override
    public void init() {
        super.init();
        renderSelectedFace = true;
    }

    @Override
    public void updateSelection(Minecraft mc) {
        super.updateSelection(mc);
    }

    @Override
    public boolean handleMouseWheel(double delta) {
        if (!schematicSelected || !selectedFace.getAxis().isHorizontal())
            return true;

        SchematicTransformation transformation = schematicHandler.getTransformation();
        Vec3 vec = Vec3.atLowerCornerOf(selectedFace.getUnitVec3i()).scale(-Math.signum(delta));
        vec = vec.multiply(transformation.getMirrorModifier(Axis.X), 1, transformation.getMirrorModifier(Axis.Z));
        vec = VecHelper.rotate(vec, transformation.getRotationTarget(), Axis.Y);
        transformation.move((int) vec.x, 0, (int) vec.z);
        schematicHandler.markDirty();

        return true;
    }

}

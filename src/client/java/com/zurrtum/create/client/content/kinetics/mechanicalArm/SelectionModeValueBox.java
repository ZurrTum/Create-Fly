package com.zurrtum.create.client.content.kinetics.mechanicalArm;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class SelectionModeValueBox extends CenteredSideValueBoxTransform {
    public SelectionModeValueBox() {
        super((blockState, direction) -> !direction.getAxis().isVertical());
    }

    @Override
    public Vec3d getLocalOffset(BlockState state) {
        int yPos = state.get(ArmBlock.CEILING) ? 16 - 3 : 3;
        Vec3d location = VecHelper.voxelSpace(8, yPos, 15.5);
        location = VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(getSide()), Direction.Axis.Y);
        return location;
    }

    @Override
    public float getScale() {
        return super.getScale();
    }
}
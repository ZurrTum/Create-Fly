package com.zurrtum.create.client.content.kinetics.mechanicalArm;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SelectionModeValueBox extends CenteredSideValueBoxTransform {
    public SelectionModeValueBox() {
        super((blockState, direction) -> !direction.getAxis().isVertical());
    }

    @Override
    public Vec3 getLocalOffset(BlockState state) {
        int yPos = state.getValue(ArmBlock.CEILING) ? 16 - 3 : 3;
        Vec3 location = VecHelper.voxelSpace(8, yPos, 15.5);
        location = VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(getSide()), Direction.Axis.Y);
        return location;
    }

    @Override
    public float getScale() {
        return super.getScale();
    }
}
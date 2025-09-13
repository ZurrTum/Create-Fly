package com.zurrtum.create.client.content.contraptions.actors.roller;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.contraptions.actors.roller.RollerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;

public class RollerValueBox extends ValueBoxTransform {

    private final int hOffset;

    public RollerValueBox(int hOffset) {
        this.hOffset = hOffset;
    }

    @Override
    public void rotate(WorldAccess level, BlockPos pos, BlockState state, MatrixStack ms) {
        Direction facing = state.get(RollerBlock.FACING);
        float yRot = AngleHelper.horizontalAngle(facing) + 180;
        TransformStack.of(ms).rotateYDegrees(yRot).rotateXDegrees(90);
    }

    @Override
    public boolean testHit(WorldAccess level, BlockPos pos, BlockState state, Vec3d localHit) {
        Vec3d offset = getLocalOffset(level, pos, state);
        if (offset == null)
            return false;
        return localHit.distanceTo(offset) < scale / 3;
    }

    @Override
    public Vec3d getLocalOffset(WorldAccess level, BlockPos pos, BlockState state) {
        Direction facing = state.get(RollerBlock.FACING);
        float stateAngle = AngleHelper.horizontalAngle(facing) + 180;
        return VecHelper.rotateCentered(VecHelper.voxelSpace(8 + hOffset, 15.5f, 11), stateAngle, Axis.Y);
    }

}
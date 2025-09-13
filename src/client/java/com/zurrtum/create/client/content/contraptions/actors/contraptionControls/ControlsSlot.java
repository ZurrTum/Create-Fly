package com.zurrtum.create.client.content.contraptions.actors.contraptionControls;


import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.contraptions.actors.trainControls.ControlsBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;

public class ControlsSlot extends ValueBoxTransform.Sided {
    @Override
    public Vec3d getLocalOffset(WorldAccess level, BlockPos pos, BlockState state) {
        Direction facing = state.get(ControlsBlock.FACING);
        float yRot = AngleHelper.horizontalAngle(facing);
        return VecHelper.rotateCentered(VecHelper.voxelSpace(8, 14f, 5.5f), yRot, Axis.Y);
    }

    @Override
    public void rotate(WorldAccess level, BlockPos pos, BlockState state, MatrixStack ms) {
        Direction facing = state.get(ControlsBlock.FACING);
        float yRot = AngleHelper.horizontalAngle(facing);
        TransformStack.of(ms).rotateYDegrees(yRot + 180).rotateXDegrees(67.5f);
    }

    @Override
    public float getScale() {
        return .508f;
    }

    @Override
    protected Vec3d getSouthLocation() {
        return Vec3d.ZERO;
    }
}

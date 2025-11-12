package com.zurrtum.create.client.content.contraptions.actors.contraptionControls;


import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.contraptions.actors.trainControls.ControlsBlock;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ControlsSlot extends ValueBoxTransform.Sided {
    @Override
    public Vec3 getLocalOffset(BlockState state) {
        Direction facing = state.getValue(ControlsBlock.FACING);
        float yRot = AngleHelper.horizontalAngle(facing);
        return VecHelper.rotateCentered(VecHelper.voxelSpace(8, 14f, 5.5f), yRot, Axis.Y);
    }

    @Override
    public void rotate(BlockState state, PoseStack ms) {
        Direction facing = state.getValue(ControlsBlock.FACING);
        float yRot = AngleHelper.horizontalAngle(facing);
        TransformStack.of(ms).rotateYDegrees(yRot + 180).rotateXDegrees(67.5f);
    }

    @Override
    public float getScale() {
        return .508f;
    }

    @Override
    protected Vec3 getSouthLocation() {
        return Vec3.ZERO;
    }
}

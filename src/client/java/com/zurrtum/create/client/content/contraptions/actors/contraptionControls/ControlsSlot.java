package com.zurrtum.create.client.content.contraptions.actors.contraptionControls;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.contraptions.actors.trainControls.ControlsBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ControlsSlot extends ValueBoxTransform.Sided {
    @Override
    public Vec3 getLocalOffset(BlockState state) {
        Direction facing = state.getValue(ControlsBlock.FACING);
        float yRot = AngleHelper.horizontalAngle(facing);
        return VecHelper.rotateCentered(VecHelper.voxelSpace(8, 14.0f, 5.5f), yRot, Direction.Axis.Y);
    }

    @Override
    public void rotate(BlockState state, PoseStack ms) {
        float yRadians = Radians.horizontalPlus180(state.getValue(ControlsBlock.FACING));
        Pose pose = ms.last();
        if (yRadians != 0) {
            pose.rotate(Axis.YP, yRadians);
        }
        pose.rotate(Axis.XP, Radians.DEG_67P5);
    }

    @Override
    public float getScale() {
        return 0.508f;
    }

    @Override
    protected Vec3 getSouthLocation() {
        return Vec3.ZERO;
    }
}

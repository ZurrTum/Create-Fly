package com.zurrtum.create.client.content.fluids.pipes;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.fluids.pipes.SmartFluidPipeBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;

public class SmartPipeFilterSlot extends ValueBoxTransform {

    @Override
    public Vec3 getLocalOffset(BlockState state) {
        AttachFace face = state.getValue(SmartFluidPipeBlock.FACE);
        float horizontalAngle = AngleHelper.horizontalAngle(state.getValue(SmartFluidPipeBlock.FACING));
        float y, z;
        if (face == AttachFace.WALL) {
            horizontalAngle += 180;
            y = 11.4f;
            z = 0.55f;
        } else if (face == AttachFace.CEILING) {
            y = 0.55f;
            z = 4.6f;
        } else {
            y = 15.45f;
            z = 4.625f;
        }
        return VecHelper.rotateCentered(VecHelper.voxelSpace(8, y, z), horizontalAngle, Direction.Axis.Y);
    }

    @Override
    public float getScale() {
        return super.getScale() * 1.02f;
    }

    @Override
    public void rotate(BlockState state, PoseStack ms) {
        AttachFace face = state.getValue(SmartFluidPipeBlock.FACE);
        Direction facing = state.getValue(SmartFluidPipeBlock.FACING);
        float yRadians;
        if (face == AttachFace.WALL) {
            yRadians = Radians.horizontalPlus180(facing);
        } else {
            yRadians = Radians.horizontal(facing);
        }
        Pose pose = ms.last();
        if (yRadians != 0) {
            pose.rotate(Axis.YP, yRadians);
        }
        pose.rotate(Axis.XP, face == AttachFace.CEILING ? Radians.DEG_315 : Radians.DEG_45);
    }
}
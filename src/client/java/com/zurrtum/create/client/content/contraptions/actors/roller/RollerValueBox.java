package com.zurrtum.create.client.content.contraptions.actors.roller;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.contraptions.actors.roller.RollerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class RollerValueBox extends ValueBoxTransform {

    private final int hOffset;

    public RollerValueBox(int hOffset) {
        this.hOffset = hOffset;
    }

    @Override
    public void rotate(BlockState state, PoseStack ms) {
        Direction facing = state.getValue(RollerBlock.FACING);
        float yRot = AngleHelper.horizontalAngle(facing) + 180;
        TransformStack.of(ms).rotateYDegrees(yRot).rotateXDegrees(90);
    }

    @Override
    public boolean testHit(LevelAccessor level, BlockPos pos, BlockState state, Vec3 localHit) {
        return localHit.distanceTo(getLocalOffset(state)) < scale / 3;
    }

    @Override
    public Vec3 getLocalOffset(BlockState state) {
        Direction facing = state.getValue(RollerBlock.FACING);
        float stateAngle = AngleHelper.horizontalAngle(facing) + 180;
        return VecHelper.rotateCentered(VecHelper.voxelSpace(8 + hOffset, 15.5f, 11), stateAngle, Axis.Y);
    }

}
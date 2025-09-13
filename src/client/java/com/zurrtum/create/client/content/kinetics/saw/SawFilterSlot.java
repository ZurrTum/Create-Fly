package com.zurrtum.create.client.content.kinetics.saw;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.kinetics.saw.SawBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;

public class SawFilterSlot extends ValueBoxTransform {
    @Override
    public Vec3d getLocalOffset(WorldAccess level, BlockPos pos, BlockState state) {
        if (state.get(SawBlock.FACING) != Direction.UP)
            return null;
        int offset = state.get(SawBlock.FLIPPED) ? -3 : 3;
        Vec3d x = VecHelper.voxelSpace(8, 12.5f, 8 + offset);
        Vec3d z = VecHelper.voxelSpace(8 + offset, 12.5f, 8);
        return state.get(SawBlock.AXIS_ALONG_FIRST_COORDINATE) ? z : x;
    }

    @Override
    public void rotate(WorldAccess level, BlockPos pos, BlockState state, MatrixStack ms) {
        int yRot = (state.get(SawBlock.AXIS_ALONG_FIRST_COORDINATE) ? 90 : 0) + (state.get(SawBlock.FLIPPED) ? 0 : 180);
        TransformStack.of(ms).rotateYDegrees(yRot).rotateXDegrees(90);
    }
}

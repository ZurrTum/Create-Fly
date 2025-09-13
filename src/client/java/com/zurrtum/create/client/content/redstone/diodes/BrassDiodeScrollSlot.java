package com.zurrtum.create.client.content.redstone.diodes;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;

public class BrassDiodeScrollSlot extends ValueBoxTransform {

    @Override
    public Vec3d getLocalOffset(WorldAccess level, BlockPos pos, BlockState state) {
        return VecHelper.voxelSpace(8, 2.6f, 8);
    }

    @Override
    public void rotate(WorldAccess level, BlockPos pos, BlockState state, MatrixStack ms) {
        float yRot = AngleHelper.horizontalAngle(state.get(Properties.HORIZONTAL_FACING)) + 180;
        TransformStack.of(ms).rotateYDegrees(yRot).rotateXDegrees(90);
    }

    @Override
    public int getOverrideColor() {
        return 0xFF592424;
    }

}

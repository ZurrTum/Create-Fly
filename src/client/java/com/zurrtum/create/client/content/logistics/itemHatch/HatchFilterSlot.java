package com.zurrtum.create.client.content.logistics.itemHatch;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.logistics.itemHatch.ItemHatchBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;

public class HatchFilterSlot extends ValueBoxTransform {

    @Override
    public Vec3d getLocalOffset(BlockState state) {
        return VecHelper.rotateCentered(VecHelper.voxelSpace(8, 5.15, 9.5), angle(state), Direction.Axis.Y);
    }

    @Override
    public float getScale() {
        return super.getScale() * 0.965f;
    }

    public boolean testHit(WorldAccess level, BlockPos pos, BlockState state, Vec3d localHit) {
        return localHit.distanceTo(getLocalOffset(state).subtract(0, 0.125, 0)) < scale / 2;
    }

    @Override
    public void rotate(BlockState state, MatrixStack ms) {
        ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle(state)));
        ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-45));
    }

    private float angle(BlockState state) {
        return AngleHelper.horizontalAngle(state.get(ItemHatchBlock.FACING, Direction.NORTH));
    }

}

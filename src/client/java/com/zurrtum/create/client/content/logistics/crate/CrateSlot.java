package com.zurrtum.create.client.content.logistics.crate;

import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;

public class CrateSlot extends ValueBoxTransform {
    @Override
    public void rotate(WorldAccess level, BlockPos pos, BlockState state, MatrixStack ms) {
        TransformStack.of(ms).rotateXDegrees(90);
    }

    @Override
    public Vec3d getLocalOffset(WorldAccess level, BlockPos pos, BlockState state) {
        return new Vec3d(0.5, 13.5 / 16d, 0.5);
    }
}

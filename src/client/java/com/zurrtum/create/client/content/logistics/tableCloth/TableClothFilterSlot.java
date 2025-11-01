package com.zurrtum.create.client.content.logistics.tableCloth;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.logistics.tableCloth.TableClothBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;

class TableClothFilterSlot extends ValueBoxTransform {

    private final TableClothBlockEntity be;

    public TableClothFilterSlot(TableClothBlockEntity be) {
        this.be = be;
    }

    @Override
    public Vec3d getLocalOffset(BlockState state) {
        Vec3d v = be.sideOccluded ? VecHelper.voxelSpace(8, 0.75, 15.25) : VecHelper.voxelSpace(12, -2.75, 16.75);
        return VecHelper.rotateCentered(v, -be.facing.getPositiveHorizontalDegrees(), Axis.Y);
    }

    @Override
    public void rotate(BlockState state, MatrixStack ms) {
        TransformStack.of(ms).rotateYDegrees(180 - be.facing.getPositiveHorizontalDegrees()).rotateXDegrees(be.sideOccluded ? 90 : 0);
    }

}

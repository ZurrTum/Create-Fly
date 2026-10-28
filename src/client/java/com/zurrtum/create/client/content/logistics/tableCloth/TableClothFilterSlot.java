package com.zurrtum.create.client.content.logistics.tableCloth;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.logistics.tableCloth.TableClothBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

class TableClothFilterSlot extends ValueBoxTransform {

    private final TableClothBlockEntity be;

    public TableClothFilterSlot(TableClothBlockEntity be) {
        this.be = be;
    }

    @Override
    public Vec3 getLocalOffset(BlockState state) {
        Vec3 v = be.sideOccluded ? VecHelper.voxelSpace(8, 0.75, 15.25) : VecHelper.voxelSpace(12, -2.75, 16.75);
        return VecHelper.rotateCentered(v, -be.facing.toYRot(), Direction.Axis.Y);
    }

    @Override
    public void rotate(BlockState state, PoseStack ms) {
        float yRot = 180 - be.facing.toYRot();
        Pose pose = ms.last();
        if (yRot != 0) {
            pose.rotate(Axis.YP, Mth.DEG_TO_RAD * yRot);
        }
        if (be.sideOccluded) {
            pose.rotate(Axis.XP, Radians.DEG_90);
        }
    }

}

package com.zurrtum.create.client.content.redstone.link;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.redstone.link.RedstoneLinkBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class RedstoneLinkFrequencySlot extends ValueBoxTransform.Dual {

    public RedstoneLinkFrequencySlot(boolean first) {
        super(first);
    }

    Vec3 horizontal = VecHelper.voxelSpace(10.0f, 5.5f, 2.5f);
    Vec3 vertical = VecHelper.voxelSpace(10.0f, 2.5f, 5.5f);

    @Override
    public Vec3 getLocalOffset(BlockState state) {
        Direction facing = state.getValue(RedstoneLinkBlock.FACING);
        Vec3 location = VecHelper.voxelSpace(8.0f, 3.01f, 5.5f);

        if (facing.getAxis().isHorizontal()) {
            location = VecHelper.voxelSpace(8.0f, 5.5f, 3.01f);
            if (isFirst()) {
                location = location.add(0, 5 / 16.0f, 0);
            }
            return rotateHorizontally(state, location);
        }

        if (isFirst()) {
            location = location.add(0, 0, 5 / 16.0f);
        }
        location = VecHelper.rotateCentered(location, facing == Direction.DOWN ? 180 : 0, Direction.Axis.X);
        return location;
    }

    @Override
    public void rotate(BlockState state, PoseStack ms) {
        Direction facing = state.getValue(RedstoneLinkBlock.FACING);
        if (facing == Direction.UP) {
            ms.rotate(Axis.XP, Radians.DEG_90);
        } else if (facing == Direction.DOWN) {
            ms.rotate(Axis.XP, Radians.DEG_270);
        } else {
            ms.rotate(Axis.YP, Radians.horizontalPlus180(facing));
        }
    }

    @Override
    public float getScale() {
        return 0.4975f;
    }

}


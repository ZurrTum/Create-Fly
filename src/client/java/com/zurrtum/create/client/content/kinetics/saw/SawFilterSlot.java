package com.zurrtum.create.client.content.kinetics.saw;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.kinetics.saw.SawBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SawFilterSlot extends ValueBoxTransform {
    @Override
    @Nullable
    public Vec3 getLocalOffset(BlockState state) {
        if (state.getValue(SawBlock.FACING) != Direction.UP) {
            return null;
        }
        int offset = state.getValue(SawBlock.FLIPPED) ? -3 : 3;
        Vec3 x = VecHelper.voxelSpace(8, 12.5f, 8 + offset);
        Vec3 z = VecHelper.voxelSpace(8 + offset, 12.5f, 8);
        return state.getValue(SawBlock.AXIS_ALONG_FIRST_COORDINATE) ? z : x;
    }

    @Override
    public void rotate(BlockState state, PoseStack ms) {
        float yRadians = (state.getValue(SawBlock.AXIS_ALONG_FIRST_COORDINATE) ? Radians.DEG_90 : 0) + (
            state.getValue(SawBlock.FLIPPED) ? 0 : Radians.DEG_180);
        Pose pose = ms.last();
        if (yRadians != 0) {
            pose.rotate(Axis.YP, yRadians);
        }
        pose.rotate(Axis.XP, Radians.DEG_90);
    }
}

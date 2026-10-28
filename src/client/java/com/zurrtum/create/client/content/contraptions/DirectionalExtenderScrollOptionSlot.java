package com.zurrtum.create.client.content.contraptions;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiPredicate;

public class DirectionalExtenderScrollOptionSlot extends CenteredSideValueBoxTransform {

    public DirectionalExtenderScrollOptionSlot(BiPredicate<BlockState, Direction> allowedDirections) {
        super(allowedDirections);
    }

    @Override
    public Vec3 getLocalOffset(BlockState state) {
        return super.getLocalOffset(state)
            .add(Vec3.atLowerCornerOf(state.getValue(BlockStateProperties.FACING).getUnitVec3i()).scale(-2 / 16.0f));
    }

    @Override
    public void rotate(BlockState state, PoseStack ms) {
        if (!getSide().getAxis().isHorizontal()) {
            float yRadians = Radians.horizontalPlus180(state.getValue(BlockStateProperties.FACING));
            if (yRadians != 0) {
                ms.rotate(Axis.YP, yRadians);
            }
        }
        super.rotate(state, ms);
    }
}

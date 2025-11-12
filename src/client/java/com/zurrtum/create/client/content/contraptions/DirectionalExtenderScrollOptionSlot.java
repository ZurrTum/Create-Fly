package com.zurrtum.create.client.content.contraptions;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;

import java.util.function.BiPredicate;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class DirectionalExtenderScrollOptionSlot extends CenteredSideValueBoxTransform {

    public DirectionalExtenderScrollOptionSlot(BiPredicate<BlockState, Direction> allowedDirections) {
        super(allowedDirections);
    }

    @Override
    public Vec3 getLocalOffset(BlockState state) {
        return super.getLocalOffset(state).add(Vec3.atLowerCornerOf(state.getValue(BlockStateProperties.FACING).getUnitVec3i()).scale(-2 / 16f));
    }

    @Override
    public void rotate(BlockState state, PoseStack ms) {
        if (!getSide().getAxis().isHorizontal())
            TransformStack.of(ms).rotateYDegrees(AngleHelper.horizontalAngle(state.getValue(BlockStateProperties.FACING)) + 180);
        super.rotate(state, ms);
    }
}

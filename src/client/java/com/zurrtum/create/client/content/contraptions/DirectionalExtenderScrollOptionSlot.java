package com.zurrtum.create.client.content.contraptions;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.function.BiPredicate;

public class DirectionalExtenderScrollOptionSlot extends CenteredSideValueBoxTransform {

    public DirectionalExtenderScrollOptionSlot(BiPredicate<BlockState, Direction> allowedDirections) {
        super(allowedDirections);
    }

    @Override
    public Vec3d getLocalOffset(BlockState state) {
        return super.getLocalOffset(state).add(Vec3d.of(state.get(Properties.FACING).getVector()).multiply(-2 / 16f));
    }

    @Override
    public void rotate(BlockState state, MatrixStack ms) {
        if (!getSide().getAxis().isHorizontal())
            TransformStack.of(ms).rotateYDegrees(AngleHelper.horizontalAngle(state.get(Properties.FACING)) + 180);
        super.rotate(state, ms);
    }
}

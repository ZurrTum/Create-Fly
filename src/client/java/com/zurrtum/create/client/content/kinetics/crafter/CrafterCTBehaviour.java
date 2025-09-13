package com.zurrtum.create.client.content.kinetics.crafter;

import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour;
import com.zurrtum.create.content.kinetics.crafter.CrafterHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import static com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;

public class CrafterCTBehaviour extends ConnectedTextureBehaviour.Base {

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockRenderView reader, BlockPos pos, BlockPos otherPos, Direction face) {
        if (state.getBlock() != other.getBlock())
            return false;
        if (state.get(HORIZONTAL_FACING) != other.get(HORIZONTAL_FACING))
            return false;
        return CrafterHelper.areCraftersConnected(reader, pos, otherPos);
    }

    @Override
    protected boolean reverseUVs(BlockState state, Direction direction) {
        if (!direction.getAxis().isVertical())
            return false;
        Direction facing = state.get(HORIZONTAL_FACING);
        if (facing.getAxis() == direction.getAxis())
            return false;

        boolean isNegative = facing.getDirection() == AxisDirection.NEGATIVE;
        if (direction == Direction.DOWN && facing.getAxis() == Axis.Z)
            return !isNegative;
        return isNegative;
    }

    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable Sprite sprite) {
        Direction facing = state.get(HORIZONTAL_FACING);
        boolean isFront = facing.getAxis() == direction.getAxis();
        boolean isVertical = direction.getAxis().isVertical();
        boolean facingX = facing.getAxis() == Axis.X;
        return isFront ? AllSpriteShifts.BRASS_CASING : isVertical && !facingX ? AllSpriteShifts.CRAFTER_OTHERSIDE : AllSpriteShifts.CRAFTER_SIDE;
    }

}

package com.zurrtum.create.client.content.kinetics.simpleRelays.encased;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.client.content.decoration.encasing.EncasedCTBehaviour;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.simpleRelays.ICogWheel;
import com.zurrtum.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.state.property.Properties.AXIS;

public class EncasedCogCTBehaviour extends EncasedCTBehaviour {

    private Couple<CTSpriteShiftEntry> sideShifts;
    private boolean large;

    public EncasedCogCTBehaviour(CTSpriteShiftEntry shift, Couple<CTSpriteShiftEntry> sideShifts) {
        super(shift);
        large = sideShifts == null;
        this.sideShifts = sideShifts;
    }

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockRenderView reader, BlockPos pos, BlockPos otherPos, Direction face) {
        Axis axis = state.get(AXIS);
        if (large || axis == face.getAxis())
            return super.connectsTo(state, other, reader, pos, otherPos, face);

        if (other.getBlock() == state.getBlock() && other.get(AXIS) == state.get(AXIS))
            return true;

        BlockState blockState = reader.getBlockState(otherPos.offset(face));
        if (!ICogWheel.isLargeCog(blockState))
            return false;

        return ((IRotate) blockState.getBlock()).getRotationAxis(blockState) == axis;
    }

    @Override
    protected boolean reverseUVs(BlockState state, Direction face) {
        return state.get(AXIS).isHorizontal() && face.getAxis().isHorizontal() && face.getDirection() == AxisDirection.POSITIVE;
    }

    @Override
    protected boolean reverseUVsVertically(BlockState state, Direction face) {
        if (!large && state.get(AXIS) == Axis.X && face.getAxis() == Axis.Z)
            return face != Direction.SOUTH;
        return super.reverseUVsVertically(state, face);
    }

    @Override
    protected boolean reverseUVsHorizontally(BlockState state, Direction face) {
        if (large)
            return super.reverseUVsHorizontally(state, face);

        if (state.get(AXIS).isVertical() && face.getAxis().isHorizontal())
            return true;

        if (state.get(AXIS) == Axis.Z && face == Direction.DOWN)
            return true;

        return super.reverseUVsHorizontally(state, face);
    }

    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable Sprite sprite) {
        Axis axis = state.get(AXIS);
        if (large || axis == direction.getAxis()) {
            if (axis == direction.getAxis() && state.get(direction.getDirection() == AxisDirection.POSITIVE ? EncasedCogwheelBlock.TOP_SHAFT : EncasedCogwheelBlock.BOTTOM_SHAFT))
                return null;
            return super.getShift(state, direction, sprite);
        }
        return sideShifts.get(axis == Axis.X || axis == Axis.Z && direction.getAxis() == Axis.X);
    }

}

package com.zurrtum.create.client.content.kinetics.simpleRelays.encased;

import com.zurrtum.create.client.content.decoration.encasing.EncasedCTBehaviour;
import com.zurrtum.create.client.foundation.block.connected.AllCTTypes;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import com.zurrtum.create.client.foundation.block.connected.CTType;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.simpleRelays.ICogWheel;
import com.zurrtum.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS;

public class EncasedCogCTBehaviour extends EncasedCTBehaviour {
    private final CTSpriteShiftEntry side;
    private final CTSpriteShiftEntry border;

    public EncasedCogCTBehaviour(CTSpriteShiftEntry shift, CTSpriteShiftEntry side, CTSpriteShiftEntry border) {
        super(shift);
        this.side = side;
        this.border = border;
    }

    @Override
    public boolean connectsTo(
        BlockState state,
        BlockState other,
        BlockAndTintGetter reader,
        BlockPos pos,
        BlockPos otherPos,
        Direction face
    ) {
        Axis axis = state.getValue(AXIS);
        if (axis == face.getAxis()) {
            return super.connectsTo(state, other, reader, pos, otherPos, face);
        }

        if (other.getBlock() == state.getBlock() && other.getValue(AXIS) == state.getValue(AXIS)) {
            return true;
        }

        BlockState blockState = reader.getBlockState(otherPos.relative(face));
        if (!ICogWheel.isLargeCog(blockState)) {
            return false;
        }

        return ((IRotate) blockState.getBlock()).getRotationAxis(blockState) == axis;
    }

    @Override
    protected boolean reverseUVs(BlockState state, Direction face) {
        return state.getValue(AXIS).isHorizontal() && face.getAxis()
            .isHorizontal() && face.getAxisDirection() == AxisDirection.POSITIVE;
    }

    @Override
    protected boolean reverseUVsVertically(BlockState state, Direction face) {
        if (state.getValue(AXIS) == Axis.X && face.getAxis() == Axis.Z) {
            return face != Direction.SOUTH;
        }
        return super.reverseUVsVertically(state, face);
    }

    @Override
    protected boolean reverseUVsHorizontally(BlockState state, Direction face) {
        if (state.getValue(AXIS).isVertical() && face.getAxis().isHorizontal()) {
            return true;
        }

        if (state.getValue(AXIS) == Axis.Z && face == Direction.DOWN) {
            return true;
        }

        return super.reverseUVsHorizontally(state, face);
    }

    @Override
    @Nullable
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
        if (state.getValue(AXIS) == direction.getAxis()) {
            if (state.getValue(direction.getAxisDirection() == AxisDirection.POSITIVE ? EncasedCogwheelBlock.TOP_SHAFT :
                EncasedCogwheelBlock.BOTTOM_SHAFT)) {
                return null;
            }
            return super.getShift(state, direction, sprite);
        }
        return sprite != border.getOriginal() ? side : border;
    }

    @Override
    @Nullable
    public CTType getDataType(BlockAndTintGetter world, BlockPos pos, BlockState state, Direction direction) {
        Axis axis = state.getValue(AXIS);
        if (axis == direction.getAxis()) {
            if (state.getValue(direction.getAxisDirection() == AxisDirection.POSITIVE ? EncasedCogwheelBlock.TOP_SHAFT :
                EncasedCogwheelBlock.BOTTOM_SHAFT)) {
                return null;
            }
            return AllCTTypes.OMNIDIRECTIONAL;
        }
        return axis == Axis.X || axis == Axis.Z && direction.getAxis() == Axis.X ? AllCTTypes.VERTICAL :
            AllCTTypes.HORIZONTAL;
    }
}

package com.zurrtum.create.client.foundation.block.connected;

import com.zurrtum.create.foundation.block.AppearanceControlBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public abstract class ConnectedTextureBehaviour {
    @Nullable
    public CTSpriteShiftEntry getShift(
        BlockState state,
        RandomSource rand,
        Direction direction,
        TextureAtlasSprite sprite
    ) {
        return getShift(state, direction, sprite);
    }

    @Nullable
    public abstract CTSpriteShiftEntry getShift(BlockState state, Direction direction, TextureAtlasSprite sprite);

    // TODO: allow more than one data type per state/face?
    @Nullable
    public abstract CTType getDataType(BlockAndTintGetter world, BlockPos pos, BlockState state, Direction direction);

    public boolean buildContextForOccludedDirections() {
        return false;
    }

    protected boolean isBeingBlocked(
        BlockState state,
        BlockAndTintGetter reader,
        BlockPos pos,
        BlockPos otherPos,
        Direction face
    ) {
        BlockPos blockingPos = otherPos.relative(face);
        BlockState blockState = reader.getBlockState(pos);
        BlockState blockingState = reader.getBlockState(blockingPos);

        if (!Block.isFaceFull(blockingState.getShape(reader, blockingPos), face.getOpposite())) {
            return false;
        }
        if (face.getAxis().choose(pos.getX(), pos.getY(), pos.getZ()) != face.getAxis()
            .choose(otherPos.getX(), otherPos.getY(), otherPos.getZ())) {
            return false;
        }

        return connectsTo(
            state,
            getCTBlockState(reader, blockState, face.getOpposite(), pos.relative(face), blockingPos),
            reader,
            pos,
            blockingPos,
            face
        );
    }

    public boolean connectsTo(
        BlockState state,
        BlockState other,
        BlockAndTintGetter reader,
        BlockPos pos,
        BlockPos otherPos,
        Direction face,
        @Nullable Direction primaryOffset,
        @Nullable Direction secondaryOffset
    ) {
        return connectsTo(state, other, reader, pos, otherPos, face);
    }

    public boolean connectsTo(
        BlockState state,
        BlockState other,
        BlockAndTintGetter reader,
        BlockPos pos,
        BlockPos otherPos,
        Direction face
    ) {
        return !isBeingBlocked(state, reader, pos, otherPos, face) && state.getBlock() == other.getBlock();
    }

    public boolean testConnection(
        BlockAndTintGetter reader,
        BlockPos currentPos,
        BlockPos targetPos,
        BlockState trueCurrentState,
        BlockState connectiveCurrentState,
        Direction textureSide,
        @Nullable Direction horizontal,
        @Nullable Direction vertical
    ) {
        return connectsTo(
            connectiveCurrentState,
            getCTBlockState(reader, trueCurrentState, textureSide, currentPos, targetPos),
            reader,
            currentPos,
            targetPos,
            textureSide,
            horizontal,
            vertical
        );
    }

    public BlockState getCTBlockState(
        BlockAndTintGetter reader,
        BlockState reference,
        Direction face,
        BlockPos fromPos,
        BlockPos toPos
    ) {
        BlockState blockState = reader.getBlockState(toPos);
        if (blockState.getBlock() instanceof AppearanceControlBlock block) {
            return block.getAppearance(blockState, reader, toPos, face, reference, fromPos);
        }
        return blockState;
    }

    protected boolean reverseUVs(BlockState state, Direction face) {
        return false;
    }

    protected boolean reverseUVsHorizontally(BlockState state, Direction face) {
        return reverseUVs(state, face);
    }

    protected boolean reverseUVsVertically(BlockState state, Direction face) {
        return reverseUVs(state, face);
    }

    protected Direction getUpDirection(BlockAndTintGetter reader, BlockPos pos, BlockState state, Direction face) {
        Axis axis = face.getAxis();
        return axis.isHorizontal() ? Direction.UP : Direction.NORTH;
    }

    protected Direction getRightDirection(BlockAndTintGetter reader, BlockPos pos, BlockState state, Direction face) {
        Axis axis = face.getAxis();
        return axis == Axis.X ? Direction.SOUTH : Direction.WEST;
    }

    public int buildContext(BlockAndTintGetter reader, BlockPos pos, BlockState state, Direction face, CTType type) {
        Direction horizontal = getRightDirection(reader, pos, state, face);
        Direction vertical = getUpDirection(reader, pos, state, face);
        boolean down = face == Direction.DOWN;
        if (down) {
            vertical = vertical.getOpposite();
        }
        if (down ^ face.getAxisDirection() == AxisDirection.POSITIVE) {
            horizontal = horizontal.getOpposite();
        }
        BlockState trueCurrentState = reader.getBlockState(pos);
        boolean flipH = reverseUVsHorizontally(state, face);
        boolean flipV = reverseUVsVertically(state, face);
        MutableBlockPos targetPos = new MutableBlockPos();
        int context = 0;
        for (CTPosStep step : type.getSteps()) {
            if (step.test(
                this,
                context,
                flipH,
                flipV,
                reader,
                pos,
                targetPos,
                trueCurrentState,
                state,
                face,
                horizontal,
                vertical
            )) {
                context |= step.flag();
            }
        }
        return context;
    }

    public static abstract class Base extends ConnectedTextureBehaviour {
        @Override
        @Nullable
        public abstract CTSpriteShiftEntry getShift(
            BlockState state,
            Direction direction,
            @Nullable TextureAtlasSprite sprite
        );

        @Override
        @Nullable
        public CTType getDataType(BlockAndTintGetter world, BlockPos pos, BlockState state, Direction direction) {
            CTSpriteShiftEntry shift = getShift(state, direction, null);
            if (shift == null) {
                return null;
            }
            return shift.getType();
        }
    }
}

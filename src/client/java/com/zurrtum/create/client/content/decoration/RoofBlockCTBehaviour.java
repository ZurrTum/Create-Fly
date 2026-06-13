package com.zurrtum.create.client.content.decoration;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.foundation.block.connected.*;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import org.jspecify.annotations.Nullable;

public class RoofBlockCTBehaviour extends ConnectedTextureBehaviour.Base {

    private final CTSpriteShiftEntry shift;

    public RoofBlockCTBehaviour(CTSpriteShiftEntry shift) {
        this.shift = shift;
    }

    @Override
    public @Nullable CTSpriteShiftEntry getShift(
        BlockState state,
        Direction direction,
        @Nullable TextureAtlasSprite sprite
    ) {
        if (direction == Direction.UP) {
            return shift;
        }
        return null;
    }

    @Override
    public boolean buildContextForOccludedDirections() {
        return true;
    }

    @Override
    public int buildContext(BlockAndTintGetter reader, BlockPos pos, BlockState state, Direction face, CTType type) {
        if (isUprightStair(state)) {
            return ((RoofCTType) type).getStairMapping(state);
        }
        return super.buildContext(reader, pos, state, face, type);
    }

    @Override
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
        if (connects(reader, pos, state, other) || connectsHigh(
            reader,
            pos,
            state,
            other,
            reader.getBlockState(otherPos.above())
        )) {
            return true;
        }
        if (primaryOffset != null && secondaryOffset != null) {
            return false;
        }

        for (boolean p : Iterate.trueAndFalse) {
            Direction offset = p ? primaryOffset : secondaryOffset;
            if (offset == null) {
                continue;
            }
            if (offset.getAxis().isVertical()) {
                continue;
            }

            if (connectsHigh(
                reader,
                pos,
                state,
                reader.getBlockState(pos.relative(offset.getClockWise())),
                reader.getBlockState(pos.relative(offset.getClockWise()).above())
            ) || connectsHigh(
                reader,
                pos,
                state,
                reader.getBlockState(pos.relative(offset.getCounterClockWise())),
                reader.getBlockState(pos.relative(offset.getCounterClockWise()).above())
            )) {
                return true;
            }
        }

        return false;
    }

    public boolean isUprightStair(BlockState state) {
        return state.hasProperty(StairBlock.SHAPE) && state.getValueOrElse(StairBlock.HALF, Half.TOP) == Half.BOTTOM;
    }

    protected boolean connects(BlockAndTintGetter reader, BlockPos pos, BlockState state, BlockState other) {
        double top = state.getCollisionShape(reader, pos).max(Axis.Y);
        double topOther =
            other.getSoundType() != SoundType.COPPER ? 0 : other.getCollisionShape(reader, pos).max(Axis.Y);
        return Mth.equal(top, topOther);
    }

    protected boolean connectsHigh(
        BlockAndTintGetter reader,
        BlockPos pos,
        BlockState state,
        BlockState other,
        BlockState aboveOther
    ) {
        if (state.getBlock() instanceof SlabBlock && other.getBlock() instanceof SlabBlock) {
            if (state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM && other.getValue(SlabBlock.TYPE) != SlabType.BOTTOM) {
                return true;
            }
        }

        if (state.getBlock() instanceof SlabBlock && state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM) {
            double top = state.getCollisionShape(reader, pos).max(Axis.Y);
            double topOther = other.getCollisionShape(reader, pos).max(Axis.Y);
            return !Mth.equal(top, topOther) && topOther > top;
        }

        double topAboveOther = aboveOther.getCollisionShape(reader, pos).max(Axis.Y);
        return topAboveOther > 0;
    }

    @Override
    public CTType getDataType(BlockAndTintGetter world, BlockPos pos, BlockState state, Direction direction) {
        return AllCTTypes.ROOF;
    }

}
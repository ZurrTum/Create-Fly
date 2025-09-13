package com.zurrtum.create.client.content.decoration;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.foundation.block.connected.AllCTTypes;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import com.zurrtum.create.client.foundation.block.connected.CTType;
import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.block.enums.StairShape;
import net.minecraft.client.texture.Sprite;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

public class RoofBlockCTBehaviour extends ConnectedTextureBehaviour.Base {

    private final CTSpriteShiftEntry shift;

    public RoofBlockCTBehaviour(CTSpriteShiftEntry shift) {
        this.shift = shift;
    }

    @Override
    public @Nullable CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable Sprite sprite) {
        if (direction == Direction.UP)
            return shift;
        return null;
    }

    @Override
    public boolean buildContextForOccludedDirections() {
        return true;
    }

    @Override
    public CTContext buildContext(BlockRenderView reader, BlockPos pos, BlockState state, Direction face, ContextRequirement requirement) {

        if (isUprightStair(state))
            return getStairMapping(state);

        return super.buildContext(reader, pos, state, face, requirement);
    }

    @Override
    public boolean connectsTo(
        BlockState state,
        BlockState other,
        BlockRenderView reader,
        BlockPos pos,
        BlockPos otherPos,
        Direction face,
        Direction primaryOffset,
        Direction secondaryOffset
    ) {

        if (connects(reader, pos, state, other) || connectsHigh(reader, pos, state, other, reader.getBlockState(otherPos.up())))
            return true;
        if (primaryOffset != null && secondaryOffset != null)
            return false;

        for (boolean p : Iterate.trueAndFalse) {
            Direction offset = p ? primaryOffset : secondaryOffset;
            if (offset == null)
                continue;
            if (offset.getAxis().isVertical())
                continue;

            if (connectsHigh(
                reader,
                pos,
                state,
                reader.getBlockState(pos.offset(offset.rotateYClockwise())),
                reader.getBlockState(pos.offset(offset.rotateYClockwise()).up())
            ) || connectsHigh(
                reader,
                pos,
                state,
                reader.getBlockState(pos.offset(offset.rotateYCounterclockwise())),
                reader.getBlockState(pos.offset(offset.rotateYCounterclockwise()).up())
            ))
                return true;
        }

        return false;
    }

    public boolean isUprightStair(BlockState state) {
        return state.contains(StairsBlock.SHAPE) && state.get(StairsBlock.HALF, BlockHalf.TOP) == BlockHalf.BOTTOM;
    }

    public CTContext getStairMapping(BlockState state) {
        CTContext context = new CTContext();
        StairShape shape = state.get(StairsBlock.SHAPE);
        Direction facing = state.get(StairsBlock.FACING);

        if (shape == StairShape.OUTER_LEFT)
            facing = facing.rotateYCounterclockwise();
        if (shape == StairShape.INNER_LEFT)
            facing = facing.rotateYCounterclockwise();

        int type = shape == StairShape.STRAIGHT ? 0 : (shape == StairShape.INNER_LEFT || shape == StairShape.INNER_RIGHT) ? 1 : 2;
        int rot = facing.getHorizontalQuarterTurns();
        context.up = type >= 2;
        context.right = type % 2 == 1;
        context.left = rot >= 2;
        context.down = rot % 2 == 1;
        return context;
    }

    protected boolean connects(BlockRenderView reader, BlockPos pos, BlockState state, BlockState other) {
        double top = state.getCollisionShape(reader, pos).getMax(Axis.Y);
        double topOther = other.getSoundGroup() != BlockSoundGroup.COPPER ? 0 : other.getCollisionShape(reader, pos).getMax(Axis.Y);
        return MathHelper.approximatelyEquals(top, topOther);
    }

    protected boolean connectsHigh(BlockRenderView reader, BlockPos pos, BlockState state, BlockState other, BlockState aboveOther) {
        if (state.getBlock() instanceof SlabBlock && other.getBlock() instanceof SlabBlock)
            if (state.get(SlabBlock.TYPE) == SlabType.BOTTOM && other.get(SlabBlock.TYPE) != SlabType.BOTTOM)
                return true;

        if (state.getBlock() instanceof SlabBlock && state.get(SlabBlock.TYPE) == SlabType.BOTTOM) {
            double top = state.getCollisionShape(reader, pos).getMax(Axis.Y);
            double topOther = other.getCollisionShape(reader, pos).getMax(Axis.Y);
            return !MathHelper.approximatelyEquals(top, topOther) && topOther > top;
        }

        double topAboveOther = aboveOther.getCollisionShape(reader, pos).getMax(Axis.Y);
        return topAboveOther > 0;
    }

    @Override
    public @Nullable CTType getDataType(BlockRenderView world, BlockPos pos, BlockState state, Direction direction) {
        return isUprightStair(state) ? AllCTTypes.ROOF_STAIR : AllCTTypes.ROOF;
    }

}
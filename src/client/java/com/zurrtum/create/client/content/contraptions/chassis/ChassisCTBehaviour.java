package com.zurrtum.create.client.content.contraptions.chassis;


import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour;
import com.zurrtum.create.content.contraptions.chassis.LinearChassisBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jspecify.annotations.Nullable;

public class ChassisCTBehaviour extends ConnectedTextureBehaviour.Base {

    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
        Block block = state.getBlock();
        BooleanProperty glueableSide = ((LinearChassisBlock) block).getGlueableSide(state, direction);
        if (glueableSide == null)
            return state.is(AllBlocks.LINEAR_CHASSIS) ? AllSpriteShifts.CHASSIS_SIDE : AllSpriteShifts.SECONDARY_CHASSIS_SIDE;
        return state.getValue(glueableSide) ? AllSpriteShifts.CHASSIS_STICKY : AllSpriteShifts.CHASSIS;
    }

    @Override
    protected Direction getUpDirection(BlockAndTintGetter reader, BlockPos pos, BlockState state, Direction face) {
        Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
        if (face.getAxis() == axis)
            return super.getUpDirection(reader, pos, state, face);
        return Direction.get(Direction.AxisDirection.POSITIVE, axis);
    }

    @Override
    protected Direction getRightDirection(BlockAndTintGetter reader, BlockPos pos, BlockState state, Direction face) {
        Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
        return axis != face.getAxis() && axis.isHorizontal() ? (face.getAxis()
            .isHorizontal() ? Direction.DOWN : (axis == Direction.Axis.X ? Direction.NORTH : Direction.EAST)) : super.getRightDirection(
            reader,
            pos,
            state,
            face
        );
    }

    @Override
    protected boolean reverseUVsHorizontally(BlockState state, Direction face) {
        Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
        boolean side = face.getAxis() != axis;
        if (side && axis == Direction.Axis.X && face.getAxis().isHorizontal())
            return true;
        return super.reverseUVsHorizontally(state, face);
    }

    @Override
    protected boolean reverseUVsVertically(BlockState state, Direction face) {
        return super.reverseUVsVertically(state, face);
    }

    @Override
    public boolean reverseUVs(BlockState state, Direction face) {
        Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
        boolean end = face.getAxis() == axis;
        if (end && axis.isHorizontal() && (face.getAxisDirection() == Direction.AxisDirection.POSITIVE))
            return true;
        if (!end && axis.isHorizontal() && face == Direction.DOWN)
            return true;
        return super.reverseUVs(state, face);
    }

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos, BlockPos otherPos, Direction face) {
        Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
        boolean superConnect = face.getAxis() == axis ? super.connectsTo(state, other, reader, pos, otherPos, face) : state.is(other.getBlock());
        return superConnect && axis == other.getValue(BlockStateProperties.AXIS);
    }

}
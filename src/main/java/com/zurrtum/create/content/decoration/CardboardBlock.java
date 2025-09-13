package com.zurrtum.create.content.decoration;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;

public class CardboardBlock extends Block {

    public static final Property<Axis> HORIZONTAL_AXIS = Properties.HORIZONTAL_AXIS;

    public CardboardBlock(Settings pProperties) {
        super(pProperties);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        return getDefaultState().with(HORIZONTAL_AXIS, pContext.getHorizontalPlayerFacing().getAxis());
    }

    //TODO
    //    @Override
    //    public int getFireSpreadSpeed(BlockState state, BlockView level, BlockPos pos, Direction direction) {
    //        return 100;
    //    }
    //TODO
    //    @Override
    //    public int getFlammability(BlockState state, BlockView level, BlockPos pos, Direction direction) {
    //        return 20;
    //    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(HORIZONTAL_AXIS));
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rot) {
        return state.with(HORIZONTAL_AXIS, rot.rotate(Direction.get(AxisDirection.POSITIVE, state.get(HORIZONTAL_AXIS))).getAxis());
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirrorIn) {
        return state;
    }

}

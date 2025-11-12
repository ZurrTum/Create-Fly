package com.zurrtum.create.content.decoration;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

public class CardboardBlock extends Block {

    public static final Property<Axis> HORIZONTAL_AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    public CardboardBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return defaultBlockState().setValue(HORIZONTAL_AXIS, pContext.getHorizontalDirection().getAxis());
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(HORIZONTAL_AXIS));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(HORIZONTAL_AXIS, rot.rotate(Direction.get(AxisDirection.POSITIVE, state.getValue(HORIZONTAL_AXIS))).getAxis());
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state;
    }

}

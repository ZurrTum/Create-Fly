package com.zurrtum.create.content.contraptions.chassis;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public class RadialChassisBlock extends AbstractChassisBlock {

    public static final BooleanProperty STICKY_NORTH = BooleanProperty.of("sticky_north");
    public static final BooleanProperty STICKY_SOUTH = BooleanProperty.of("sticky_south");
    public static final BooleanProperty STICKY_EAST = BooleanProperty.of("sticky_east");
    public static final BooleanProperty STICKY_WEST = BooleanProperty.of("sticky_west");

    public RadialChassisBlock(Settings properties) {
        super(properties);
        setDefaultState(getDefaultState().with(STICKY_EAST, false).with(STICKY_SOUTH, false).with(STICKY_NORTH, false).with(STICKY_WEST, false));
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        builder.add(STICKY_NORTH, STICKY_EAST, STICKY_SOUTH, STICKY_WEST);
        super.appendProperties(builder);
    }

    @Override
    public BooleanProperty getGlueableSide(BlockState state, Direction face) {
        Axis axis = state.get(AXIS);

        if (axis == Axis.X) {
            if (face == Direction.NORTH)
                return STICKY_WEST;
            if (face == Direction.SOUTH)
                return STICKY_EAST;
            if (face == Direction.UP)
                return STICKY_NORTH;
            if (face == Direction.DOWN)
                return STICKY_SOUTH;
        }

        if (axis == Axis.Y) {
            if (face == Direction.NORTH)
                return STICKY_NORTH;
            if (face == Direction.SOUTH)
                return STICKY_SOUTH;
            if (face == Direction.EAST)
                return STICKY_EAST;
            if (face == Direction.WEST)
                return STICKY_WEST;
        }

        if (axis == Axis.Z) {
            if (face == Direction.UP)
                return STICKY_NORTH;
            if (face == Direction.DOWN)
                return STICKY_SOUTH;
            if (face == Direction.EAST)
                return STICKY_EAST;
            if (face == Direction.WEST)
                return STICKY_WEST;
        }

        return null;
    }

}

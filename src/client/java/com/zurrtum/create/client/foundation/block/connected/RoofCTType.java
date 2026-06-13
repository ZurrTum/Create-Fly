package com.zurrtum.create.client.foundation.block.connected;

import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StairsShape;

import static com.zurrtum.create.Create.MOD_ID;

public class RoofCTType extends CTType {
    private static final int UP = UP_FLAG;
    private static final int DOWN = DOWN_FLAG;
    private static final int LEFT = LEFT_FLAG;
    private static final int RIGHT = RIGHT_FLAG;
    private static final int TOP_LEFT = TOP_LEFT_FLAG;
    private static final int TOP_RIGHT = TOP_RIGHT_FLAG;
    private static final int BOTTOM_LEFT = BOTTOM_LEFT_FLAG;
    private static final int BOTTOM_RIGHT = BOTTOM_RIGHT_FLAG;
    private static final int UP_DOWN_LEFT_RIGHT = UP | DOWN | LEFT | RIGHT;
    private static final int TOP_LEFT_RIGHT = TOP_LEFT | TOP_RIGHT;
    private static final int BOTTOM_LEFT_RIGHT = BOTTOM_LEFT | BOTTOM_RIGHT;
    private static final int INNER_LEFT = StairsShape.INNER_LEFT.ordinal() << 2;
    private static final int INNER_RIGHT = StairsShape.INNER_RIGHT.ordinal() << 2;
    private static final int OUTER_LEFT = StairsShape.OUTER_LEFT.ordinal() << 2;
    private static final int OUTER_RIGHT = StairsShape.OUTER_RIGHT.ordinal() << 2;
    private static final int SOUTH = Direction.SOUTH.get2DDataValue();
    private static final int WEST = Direction.WEST.get2DDataValue();
    private static final int NORTH = Direction.NORTH.get2DDataValue();
    private static final int EAST = Direction.EAST.get2DDataValue();

    public static final int[] MAP;
    public static final int[] STAIR_MAP;
    public static final int SIZE;

    static {
        MAP = new int[ALL_FLAGS + 1];
        STAIR_MAP = new int[20];
        int flags, index = 0;
        flags = DOWN | RIGHT | BOTTOM_RIGHT;
        MAP[flags] = ++index;
        STAIR_MAP[OUTER_LEFT | NORTH] = STAIR_MAP[OUTER_RIGHT | EAST] = flags;
        flags = DOWN | LEFT | RIGHT;
        MAP[flags | BOTTOM_LEFT] = MAP[flags | BOTTOM_RIGHT] = MAP[flags | BOTTOM_LEFT_RIGHT] = MAP[flags] = ++index;
        STAIR_MAP[SOUTH] = flags;
        flags = DOWN | LEFT | BOTTOM_LEFT;
        MAP[flags] = ++index;
        STAIR_MAP[OUTER_LEFT | EAST] = STAIR_MAP[OUTER_RIGHT | SOUTH] = flags;
        flags = UP | DOWN | RIGHT;
        MAP[flags | TOP_RIGHT] = MAP[flags | BOTTOM_RIGHT] = MAP[flags | TOP_RIGHT | BOTTOM_RIGHT] = MAP[flags] = ++index;
        STAIR_MAP[EAST] = flags;
        flags = UP | DOWN | LEFT;
        MAP[flags | TOP_LEFT] = MAP[flags | BOTTOM_LEFT] = MAP[flags | TOP_LEFT | BOTTOM_LEFT] = MAP[flags] = ++index;
        STAIR_MAP[WEST] = flags;
        flags = UP | RIGHT | TOP_RIGHT;
        MAP[flags] = ++index;
        STAIR_MAP[OUTER_LEFT | WEST] = STAIR_MAP[OUTER_RIGHT | NORTH] = flags;
        flags = UP | LEFT | RIGHT;
        MAP[flags | TOP_LEFT] = MAP[flags | TOP_RIGHT] = MAP[flags | TOP_LEFT_RIGHT] = MAP[flags] = ++index;
        STAIR_MAP[NORTH] = flags;
        flags = UP | LEFT | TOP_LEFT;
        MAP[flags] = ++index;
        STAIR_MAP[OUTER_LEFT | SOUTH] = STAIR_MAP[OUTER_RIGHT | WEST] = flags;
        flags = UP_DOWN_LEFT_RIGHT | TOP_LEFT_RIGHT | BOTTOM_LEFT;
        MAP[flags] = ++index;
        STAIR_MAP[INNER_LEFT | SOUTH] = STAIR_MAP[INNER_RIGHT | WEST] = flags;
        flags = UP_DOWN_LEFT_RIGHT | TOP_LEFT_RIGHT | BOTTOM_RIGHT;
        MAP[flags] = ++index;
        STAIR_MAP[INNER_LEFT | WEST] = STAIR_MAP[INNER_RIGHT | NORTH] = flags;
        flags = UP_DOWN_LEFT_RIGHT | TOP_LEFT | BOTTOM_LEFT_RIGHT;
        MAP[flags] = ++index;
        STAIR_MAP[INNER_LEFT | EAST] = STAIR_MAP[INNER_RIGHT | SOUTH] = flags;
        flags = UP_DOWN_LEFT_RIGHT | TOP_RIGHT | BOTTOM_LEFT_RIGHT;
        MAP[flags] = ++index;
        STAIR_MAP[INNER_LEFT | NORTH] = STAIR_MAP[INNER_RIGHT | EAST] = flags;
        SIZE = index + 1;
    }

    public RoofCTType() {
        super(Identifier.fromNamespaceAndPath(MOD_ID, "roof"), SIZE, ALL);
    }

    public int getStairMapping(BlockState state) {
        return STAIR_MAP[state.getValue(StairBlock.SHAPE).ordinal() << 2 | state.getValue(StairBlock.FACING)
            .get2DDataValue()];
    }

    @Override
    public int getTextureIndex(int context) {
        return MAP[context & ALL_FLAGS];
    }
}

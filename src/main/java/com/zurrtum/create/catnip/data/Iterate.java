package com.zurrtum.create.catnip.data;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class Iterate {
    public static final boolean[] trueAndFalse = {true, false};
    public static final boolean[] falseAndTrue = {false, true};
    public static final int[] zeroAndOne = {0, 1};
    public static final int[] positiveAndNegative = {1, -1};
    public static final Direction[] directions = Direction.values();
    public static final Direction[] horizontalDirections = getHorizontals();
    public static final Axis[] axes = Axis.values();
    public static final EnumSet<Axis> axisSet = EnumSet.allOf(Axis.class);

    private static Direction[] getHorizontals() {
        Direction[] directions = new Direction[4];
        for (int i = 0; i < 4; i++)
            directions[i] = Direction.fromHorizontalQuarterTurns(i);
        return directions;
    }

    public static Direction[] directionsInAxis(Axis axis) {
        return switch (axis) {
            case X -> new Direction[]{Direction.EAST, Direction.WEST};
            case Y -> new Direction[]{Direction.UP, Direction.DOWN};
            default -> new Direction[]{Direction.SOUTH, Direction.NORTH};
        };
    }

    public static List<BlockPos> hereAndBelow(BlockPos pos) {
        return Arrays.asList(pos, pos.down());
    }

    public static List<BlockPos> hereBelowAndAbove(BlockPos pos) {
        return Arrays.asList(pos, pos.down(), pos.up());
    }

    public static <T> T cycleValue(List<T> list, T current) {
        int currentIndex = list.indexOf(current);
        if (currentIndex == -1) {
            throw new IllegalArgumentException("Current value not found in list");
        }
        int nextIndex = (currentIndex + 1) % list.size();
        return list.get(nextIndex);
    }

}

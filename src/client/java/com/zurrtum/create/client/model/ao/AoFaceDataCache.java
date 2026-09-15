package com.zurrtum.create.client.model.ao;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter.Cache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class AoFaceDataCache {
    private static final int DOWN_BLOCK_FLAG = 1;
    private static final int DOWN_FLAG = 1 << 1;
    private static final int DOWN_BLOCK_SHADE_FLAG = 1 << 2;
    private static final int DOWN_SHADE_FLAG = 1 << 3;
    private static final int UP_BLOCK_FLAG = 1 << 4;
    private static final int UP_FLAG = 1 << 5;
    private static final int UP_BLOCK_SHADE_FLAG = 1 << 6;
    private static final int UP_SHADE_FLAG = 1 << 7;
    private static final int NORTH_BLOCK_FLAG = 1 << 8;
    private static final int NORTH_FLAG = 1 << 9;
    private static final int NORTH_BLOCK_SHADE_FLAG = 1 << 10;
    private static final int NORTH_SHADE_FLAG = 1 << 11;
    private static final int SOUTH_BLOCK_FLAG = 1 << 12;
    private static final int SOUTH_FLAG = 1 << 13;
    private static final int SOUTH_BLOCK_SHADE_FLAG = 1 << 14;
    private static final int SOUTH_SHADE_FLAG = 1 << 15;
    private static final int WEST_BLOCK_FLAG = 1 << 16;
    private static final int WEST_FLAG = 1 << 17;
    private static final int WEST_BLOCK_SHADE_FLAG = 1 << 18;
    private static final int WEST_SHADE_FLAG = 1 << 19;
    private static final int EAST_BLOCK_FLAG = 1 << 20;
    private static final int EAST_FLAG = 1 << 21;
    private static final int EAST_BLOCK_SHADE_FLAG = 1 << 22;
    private static final int EAST_SHADE_FLAG = 1 << 23;
    private final Cache lightCache;
    private final MutableBlockPos searchPos;
    private final DownData downBlock, down, downBlockShade, downShade;
    private final UpData upBlock, up, upBlockShade, upShade;
    private final NorthData northBlock, north, northBlockShade, northShade;
    private final SouthData southBlock, south, southBlockShade, southShade;
    private final WestData westBlock, west, westBlockShade, westShade;
    private final EastData eastBlock, east, eastBlockShade, eastShade;
    private int completionFlags;

    public AoFaceDataCache(Cache lightCache, MutableBlockPos searchPos) {
        this.lightCache = lightCache;
        this.searchPos = searchPos;
        downBlock = new DownData();
        down = new DownData();
        downBlockShade = new DownData();
        downShade = new DownData();
        upBlock = new UpData();
        up = new UpData();
        upBlockShade = new UpData();
        upShade = new UpData();
        northBlock = new NorthData();
        north = new NorthData();
        northBlockShade = new NorthData();
        northShade = new NorthData();
        southBlock = new SouthData();
        south = new SouthData();
        southBlockShade = new SouthData();
        southShade = new SouthData();
        westBlock = new WestData();
        west = new WestData();
        westBlockShade = new WestData();
        westShade = new WestData();
        eastBlock = new EastData();
        east = new EastData();
        eastBlockShade = new EastData();
        eastShade = new EastData();
    }

    public void clear() {
        completionFlags = 0;
    }

    private AoFaceData computeBlock(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        AoFaceData data,
        int mask
    ) {
        if ((completionFlags & mask) == 0) {
            completionFlags |= mask;
            data.computeBlock(level, state, pos, lightCache, searchPos, level.cardinalLighting().up());
        }
        return data;
    }

    private AoFaceData compute(BlockAndTintGetter level, BlockState state, BlockPos pos, AoFaceData data, int mask) {
        if ((completionFlags & mask) == 0) {
            completionFlags |= mask;
            data.computePartial(level, state, pos, lightCache, searchPos, level.cardinalLighting().up());
        }
        return data;
    }

    private AoFaceData computeBlockShade(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        AoFaceData data,
        int mask
    ) {
        if ((completionFlags & mask) == 0) {
            completionFlags |= mask;
            data.computeBlock(level, state, pos, lightCache, searchPos);
        }
        return data;
    }

    private AoFaceData computeShade(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        AoFaceData data,
        int mask
    ) {
        if ((completionFlags & mask) == 0) {
            completionFlags |= mask;
            data.computePartial(level, state, pos, lightCache, searchPos);
        }
        return data;
    }

    public final AoFaceData computeDownBlock(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        return computeBlock(level, state, pos, downBlock, DOWN_BLOCK_FLAG);
    }

    public final AoFaceData computeDown(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        return compute(level, state, pos, down, DOWN_FLAG);
    }

    public final AoFaceData computeDownBlockShade(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        return computeBlockShade(level, state, pos, downBlockShade, DOWN_BLOCK_SHADE_FLAG);
    }

    public final AoFaceData computeDownShade(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        return computeShade(level, state, pos, downShade, DOWN_SHADE_FLAG);
    }

    public final AoFaceData computeUpBlock(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        return computeBlock(level, state, pos, upBlock, UP_BLOCK_FLAG);
    }

    public final AoFaceData computeUp(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        return compute(level, state, pos, up, UP_FLAG);
    }

    public final AoFaceData computeUpBlockShade(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        return computeBlockShade(level, state, pos, upBlockShade, UP_BLOCK_SHADE_FLAG);
    }

    public final AoFaceData computeUpShade(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        return computeShade(level, state, pos, upShade, UP_SHADE_FLAG);
    }

    public final AoFaceData computeNorthBlock(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        return computeBlock(level, state, pos, northBlock, NORTH_BLOCK_FLAG);
    }

    public final AoFaceData computeNorth(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        return compute(level, state, pos, north, NORTH_FLAG);
    }

    public final AoFaceData computeNorthBlockShade(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        return computeBlockShade(level, state, pos, northBlockShade, NORTH_BLOCK_SHADE_FLAG);
    }

    public final AoFaceData computeNorthShade(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        return computeShade(level, state, pos, northShade, NORTH_SHADE_FLAG);
    }

    public final AoFaceData computeSouthBlock(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        return computeBlock(level, state, pos, southBlock, SOUTH_BLOCK_FLAG);
    }

    public final AoFaceData computeSouth(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        return compute(level, state, pos, south, SOUTH_FLAG);
    }

    public final AoFaceData computeSouthBlockShade(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        return computeBlockShade(level, state, pos, southBlockShade, SOUTH_BLOCK_SHADE_FLAG);
    }

    public final AoFaceData computeSouthShade(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        return computeShade(level, state, pos, southShade, SOUTH_SHADE_FLAG);
    }

    public final AoFaceData computeWestBlock(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        return computeBlock(level, state, pos, westBlock, WEST_BLOCK_FLAG);
    }

    public final AoFaceData computeWest(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        return compute(level, state, pos, west, WEST_FLAG);
    }

    public final AoFaceData computeWestBlockShade(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        return computeBlockShade(level, state, pos, westBlockShade, WEST_BLOCK_SHADE_FLAG);
    }

    public final AoFaceData computeWestShade(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        return computeShade(level, state, pos, westShade, WEST_SHADE_FLAG);
    }

    public final AoFaceData computeEastBlock(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        return computeBlock(level, state, pos, eastBlock, EAST_BLOCK_FLAG);
    }

    public final AoFaceData computeEast(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        return compute(level, state, pos, east, EAST_FLAG);
    }

    public final AoFaceData computeEastBlockShade(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        return computeBlockShade(level, state, pos, eastBlockShade, EAST_BLOCK_SHADE_FLAG);
    }

    public final AoFaceData computeEastShade(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        return computeShade(level, state, pos, eastShade, EAST_SHADE_FLAG);
    }
}

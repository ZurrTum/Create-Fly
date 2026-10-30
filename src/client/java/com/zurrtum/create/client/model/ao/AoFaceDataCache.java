package com.zurrtum.create.client.model.ao;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter.Cache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public class AoFaceDataCache {
    private static final int DOWN_BLOCK_SHADE_FLAG = 1;
    private static final int DOWN_SHADE_FLAG = 1 << 1;
    private static final int UP_BLOCK_SHADE_FLAG = 1 << 2;
    private static final int UP_SHADE_FLAG = 1 << 3;
    private static final int NORTH_BLOCK_SHADE_FLAG = 1 << 4;
    private static final int NORTH_SHADE_FLAG = 1 << 5;
    private static final int SOUTH_BLOCK_SHADE_FLAG = 1 << 6;
    private static final int SOUTH_SHADE_FLAG = 1 << 7;
    private static final int WEST_BLOCK_SHADE_FLAG = 1 << 8;
    private static final int WEST_SHADE_FLAG = 1 << 9;
    private static final int EAST_BLOCK_SHADE_FLAG = 1 << 10;
    private static final int EAST_SHADE_FLAG = 1 << 11;
    private final Cache lightCache;
    private final MutableBlockPos searchPos;
    private final DownData downBlock, down, downBlockShade, downShade;
    private @UnknownNullability DownData downBlockOverride, downOverride;
    private @Nullable Direction downBlockShadeOverride, downShadeOverride;
    private final UpData upBlock, up, upBlockShade, upShade;
    private @UnknownNullability UpData upBlockOverride, upOverride;
    private @Nullable Direction upBlockShadeOverride, upShadeOverride;
    private final NorthData northBlock, north, northBlockShade, northShade;
    private @UnknownNullability NorthData northBlockOverride, northOverride;
    private @Nullable Direction northBlockShadeOverride, northShadeOverride;
    private final SouthData southBlock, south, southBlockShade, southShade;
    private @UnknownNullability SouthData southBlockOverride, southOverride;
    private @Nullable Direction southBlockShadeOverride, southShadeOverride;
    private final WestData westBlock, west, westBlockShade, westShade;
    private @UnknownNullability WestData westBlockOverride, westOverride;
    private @Nullable Direction westBlockShadeOverride, westShadeOverride;
    private final EastData eastBlock, east, eastBlockShade, eastShade;
    private @UnknownNullability EastData eastBlockOverride, eastOverride;
    private @Nullable Direction eastBlockShadeOverride, eastShadeOverride;
    private int completionFlags;
    private boolean clearOverride;

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
        if (clearOverride) {
            clearOverride = false;
            downBlockShadeOverride = null;
            downShadeOverride = null;
            upBlockShadeOverride = null;
            upShadeOverride = null;
            northBlockShadeOverride = null;
            northShadeOverride = null;
            southBlockShadeOverride = null;
            southShadeOverride = null;
            westBlockShadeOverride = null;
            westShadeOverride = null;
            eastBlockShadeOverride = null;
            eastShadeOverride = null;
        }
    }

    public final DownData computeDownBlock(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        Direction shadeDirectionOverride
    ) {
        if (downBlockShadeOverride != shadeDirectionOverride) {
            clearOverride = true;
            CardinalLighting cardinalLighting = level.cardinalLighting();
            float shadeBrightness = cardinalLighting.byFace(shadeDirectionOverride);
            if (downBlockShadeOverride == null || cardinalLighting.byFace(downBlockShadeOverride) != shadeBrightness) {
                if (cardinalLighting.down() == shadeBrightness) {
                    downBlockOverride = computeDownBlockShade(level, state, pos);
                } else {
                    downBlock.computeBlock(level, state, pos, lightCache, searchPos, shadeBrightness);
                    downBlockOverride = downBlock;
                }
            }
            downBlockShadeOverride = shadeDirectionOverride;
        }
        return downBlockOverride;
    }

    public final DownData computeDown(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        Direction shadeDirectionOverride
    ) {
        if (downShadeOverride != shadeDirectionOverride) {
            clearOverride = true;
            CardinalLighting cardinalLighting = level.cardinalLighting();
            float shadeBrightness = cardinalLighting.byFace(shadeDirectionOverride);
            if (downShadeOverride == null || cardinalLighting.byFace(downShadeOverride) != shadeBrightness) {
                if (cardinalLighting.down() == shadeBrightness) {
                    downOverride = computeDownShade(level, state, pos);
                } else {
                    down.computeBlock(level, state, pos, lightCache, searchPos, shadeBrightness);
                    downOverride = down;
                }
            }
            downShadeOverride = shadeDirectionOverride;
        }
        return downOverride;
    }

    public final DownData computeDownBlockShade(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        if ((completionFlags & DOWN_BLOCK_SHADE_FLAG) == 0) {
            completionFlags |= DOWN_BLOCK_SHADE_FLAG;
            downBlockShade.computeBlock(level, state, pos, lightCache, searchPos);
        }
        return downBlockShade;
    }

    public final DownData computeDownShade(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        if ((completionFlags & DOWN_SHADE_FLAG) == 0) {
            completionFlags |= DOWN_SHADE_FLAG;
            downShade.computePartial(level, state, pos, lightCache, searchPos);
        }
        return downShade;
    }

    public final UpData computeUpBlock(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        Direction shadeDirectionOverride
    ) {
        if (upBlockShadeOverride != shadeDirectionOverride) {
            clearOverride = true;
            CardinalLighting cardinalLighting = level.cardinalLighting();
            float shadeBrightness = cardinalLighting.byFace(shadeDirectionOverride);
            if (upBlockShadeOverride == null || cardinalLighting.byFace(upBlockShadeOverride) != shadeBrightness) {
                if (cardinalLighting.up() == shadeBrightness) {
                    upBlockOverride = computeUpBlockShade(level, state, pos);
                } else {
                    upBlock.computeBlock(level, state, pos, lightCache, searchPos, shadeBrightness);
                    upBlockOverride = upBlock;
                }
            }
            upBlockShadeOverride = shadeDirectionOverride;
        }
        return upBlockOverride;
    }

    public final UpData computeUp(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        Direction shadeDirectionOverride
    ) {
        if (upShadeOverride != shadeDirectionOverride) {
            clearOverride = true;
            CardinalLighting cardinalLighting = level.cardinalLighting();
            float shadeBrightness = cardinalLighting.byFace(shadeDirectionOverride);
            if (upShadeOverride == null || cardinalLighting.byFace(upShadeOverride) != shadeBrightness) {
                if (cardinalLighting.up() == shadeBrightness) {
                    upOverride = computeUpShade(level, state, pos);
                } else {
                    up.computeBlock(level, state, pos, lightCache, searchPos, shadeBrightness);
                    upOverride = up;
                }
            }
            upShadeOverride = shadeDirectionOverride;
        }
        return upOverride;
    }

    public final UpData computeUpBlockShade(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        if ((completionFlags & UP_BLOCK_SHADE_FLAG) == 0) {
            completionFlags |= UP_BLOCK_SHADE_FLAG;
            upBlockShade.computeBlock(level, state, pos, lightCache, searchPos);
        }
        return upBlockShade;
    }

    public final UpData computeUpShade(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        if ((completionFlags & UP_SHADE_FLAG) == 0) {
            completionFlags |= UP_SHADE_FLAG;
            upShade.computePartial(level, state, pos, lightCache, searchPos);
        }
        return upShade;
    }

    public final NorthData computeNorthBlock(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        Direction shadeDirectionOverride
    ) {
        if (northBlockShadeOverride != shadeDirectionOverride) {
            clearOverride = true;
            CardinalLighting cardinalLighting = level.cardinalLighting();
            float shadeBrightness = cardinalLighting.byFace(shadeDirectionOverride);
            if (northBlockShadeOverride == null || cardinalLighting.byFace(northBlockShadeOverride) != shadeBrightness) {
                if (cardinalLighting.north() == shadeBrightness) {
                    northBlockOverride = computeNorthBlockShade(level, state, pos);
                } else {
                    northBlock.computeBlock(level, state, pos, lightCache, searchPos, shadeBrightness);
                    northBlockOverride = northBlock;
                }
            }
            northBlockShadeOverride = shadeDirectionOverride;
        }
        return northBlockOverride;
    }

    public final NorthData computeNorth(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        Direction shadeDirectionOverride
    ) {
        if (northShadeOverride != shadeDirectionOverride) {
            clearOverride = true;
            CardinalLighting cardinalLighting = level.cardinalLighting();
            float shadeBrightness = cardinalLighting.byFace(shadeDirectionOverride);
            if (northShadeOverride == null || cardinalLighting.byFace(northShadeOverride) != shadeBrightness) {
                if (cardinalLighting.north() == shadeBrightness) {
                    northOverride = computeNorthShade(level, state, pos);
                } else {
                    north.computeBlock(level, state, pos, lightCache, searchPos, shadeBrightness);
                    northOverride = north;
                }
            }
            northShadeOverride = shadeDirectionOverride;
        }
        return northOverride;
    }

    public final NorthData computeNorthBlockShade(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        if ((completionFlags & NORTH_BLOCK_SHADE_FLAG) == 0) {
            completionFlags |= NORTH_BLOCK_SHADE_FLAG;
            northBlockShade.computeBlock(level, state, pos, lightCache, searchPos);
        }
        return northBlockShade;
    }

    public final NorthData computeNorthShade(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        if ((completionFlags & NORTH_SHADE_FLAG) == 0) {
            completionFlags |= NORTH_SHADE_FLAG;
            northShade.computePartial(level, state, pos, lightCache, searchPos);
        }
        return northShade;
    }

    public final SouthData computeSouthBlock(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        Direction shadeDirectionOverride
    ) {
        if (southBlockShadeOverride != shadeDirectionOverride) {
            clearOverride = true;
            CardinalLighting cardinalLighting = level.cardinalLighting();
            float shadeBrightness = cardinalLighting.byFace(shadeDirectionOverride);
            if (southBlockShadeOverride == null || cardinalLighting.byFace(southBlockShadeOverride) != shadeBrightness) {
                if (cardinalLighting.south() == shadeBrightness) {
                    southBlockOverride = computeSouthBlockShade(level, state, pos);
                } else {
                    southBlock.computeBlock(level, state, pos, lightCache, searchPos, shadeBrightness);
                    southBlockOverride = southBlock;
                }
            }
            southBlockShadeOverride = shadeDirectionOverride;
        }
        return southBlockOverride;
    }

    public final SouthData computeSouth(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        Direction shadeDirectionOverride
    ) {
        if (southShadeOverride != shadeDirectionOverride) {
            clearOverride = true;
            CardinalLighting cardinalLighting = level.cardinalLighting();
            float shadeBrightness = cardinalLighting.byFace(shadeDirectionOverride);
            if (southShadeOverride == null || cardinalLighting.byFace(southShadeOverride) != shadeBrightness) {
                if (cardinalLighting.south() == shadeBrightness) {
                    southOverride = computeSouthShade(level, state, pos);
                } else {
                    south.computeBlock(level, state, pos, lightCache, searchPos, shadeBrightness);
                    southOverride = south;
                }
            }
            southShadeOverride = shadeDirectionOverride;
        }
        return southOverride;
    }

    public final SouthData computeSouthBlockShade(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        if ((completionFlags & SOUTH_BLOCK_SHADE_FLAG) == 0) {
            completionFlags |= SOUTH_BLOCK_SHADE_FLAG;
            southBlockShade.computeBlock(level, state, pos, lightCache, searchPos);
        }
        return southBlockShade;
    }

    public final SouthData computeSouthShade(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        if ((completionFlags & SOUTH_SHADE_FLAG) == 0) {
            completionFlags |= SOUTH_SHADE_FLAG;
            southShade.computePartial(level, state, pos, lightCache, searchPos);
        }
        return southShade;
    }

    public final WestData computeWestBlock(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        Direction shadeDirectionOverride
    ) {
        if (westBlockShadeOverride != shadeDirectionOverride) {
            clearOverride = true;
            CardinalLighting cardinalLighting = level.cardinalLighting();
            float shadeBrightness = cardinalLighting.byFace(shadeDirectionOverride);
            if (westBlockShadeOverride == null || cardinalLighting.byFace(westBlockShadeOverride) != shadeBrightness) {
                if (cardinalLighting.west() == shadeBrightness) {
                    westBlockOverride = computeWestBlockShade(level, state, pos);
                } else {
                    westBlock.computeBlock(level, state, pos, lightCache, searchPos, shadeBrightness);
                    westBlockOverride = westBlock;
                }
            }
            westBlockShadeOverride = shadeDirectionOverride;
        }
        return westBlockOverride;
    }

    public final WestData computeWest(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        Direction shadeDirectionOverride
    ) {
        if (westShadeOverride != shadeDirectionOverride) {
            clearOverride = true;
            CardinalLighting cardinalLighting = level.cardinalLighting();
            float shadeBrightness = cardinalLighting.byFace(shadeDirectionOverride);
            if (westShadeOverride == null || cardinalLighting.byFace(westShadeOverride) != shadeBrightness) {
                if (cardinalLighting.west() == shadeBrightness) {
                    westOverride = computeWestShade(level, state, pos);
                } else {
                    west.computeBlock(level, state, pos, lightCache, searchPos, shadeBrightness);
                    westOverride = west;
                }
            }
            westShadeOverride = shadeDirectionOverride;
        }
        return westOverride;
    }

    public final WestData computeWestBlockShade(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        if ((completionFlags & WEST_BLOCK_SHADE_FLAG) == 0) {
            completionFlags |= WEST_BLOCK_SHADE_FLAG;
            westBlockShade.computeBlock(level, state, pos, lightCache, searchPos);
        }
        return westBlockShade;
    }

    public final WestData computeWestShade(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        if ((completionFlags & WEST_SHADE_FLAG) == 0) {
            completionFlags |= WEST_SHADE_FLAG;
            westShade.computePartial(level, state, pos, lightCache, searchPos);
        }
        return westShade;
    }

    public final EastData computeEastBlock(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        Direction shadeDirectionOverride
    ) {
        if (eastBlockShadeOverride != shadeDirectionOverride) {
            clearOverride = true;
            CardinalLighting cardinalLighting = level.cardinalLighting();
            float shadeBrightness = cardinalLighting.byFace(shadeDirectionOverride);
            if (eastBlockShadeOverride == null || cardinalLighting.byFace(eastBlockShadeOverride) != shadeBrightness) {
                if (cardinalLighting.east() == shadeBrightness) {
                    eastBlockOverride = computeEastBlockShade(level, state, pos);
                } else {
                    eastBlock.computeBlock(level, state, pos, lightCache, searchPos, shadeBrightness);
                    eastBlockOverride = eastBlock;
                }
            }
            eastBlockShadeOverride = shadeDirectionOverride;
        }
        return eastBlockOverride;
    }

    public final EastData computeEast(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        Direction shadeDirectionOverride
    ) {
        if (eastShadeOverride != shadeDirectionOverride) {
            clearOverride = true;
            CardinalLighting cardinalLighting = level.cardinalLighting();
            float shadeBrightness = cardinalLighting.byFace(shadeDirectionOverride);
            if (eastShadeOverride == null || cardinalLighting.byFace(eastShadeOverride) != shadeBrightness) {
                if (cardinalLighting.east() == shadeBrightness) {
                    eastOverride = computeEastShade(level, state, pos);
                } else {
                    east.computeBlock(level, state, pos, lightCache, searchPos, shadeBrightness);
                    eastOverride = east;
                }
            }
            eastShadeOverride = shadeDirectionOverride;
        }
        return eastOverride;
    }

    public final EastData computeEastBlockShade(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        if ((completionFlags & EAST_BLOCK_SHADE_FLAG) == 0) {
            completionFlags |= EAST_BLOCK_SHADE_FLAG;
            eastBlockShade.computeBlock(level, state, pos, lightCache, searchPos);
        }
        return eastBlockShade;
    }

    public final EastData computeEastShade(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        if ((completionFlags & EAST_SHADE_FLAG) == 0) {
            completionFlags |= EAST_SHADE_FLAG;
            eastShade.computePartial(level, state, pos, lightCache, searchPos);
        }
        return eastShade;
    }
}

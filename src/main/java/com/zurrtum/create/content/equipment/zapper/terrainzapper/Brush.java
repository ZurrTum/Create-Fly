package com.zurrtum.create.content.equipment.zapper.terrainzapper;

import com.zurrtum.create.infrastructure.component.PlacementOptions;
import com.zurrtum.create.infrastructure.component.TerrainTools;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;

import java.util.Collection;

public abstract class Brush {

    protected int param0;
    protected int param1;
    protected int param2;
    public int amtParams;

    public Brush(int amtParams) {
        this.amtParams = amtParams;
    }

    public void set(int param0, int param1, int param2) {
        this.param0 = param0;
        this.param1 = param1;
        this.param2 = param2;
    }

    public TerrainTools[] getSupportedTools() {
        return TerrainTools.values();
    }

    public TerrainTools redirectTool(TerrainTools tool) {
        return tool;
    }

    public boolean hasPlacementOptions() {
        return true;
    }

    public boolean hasConnectivityOptions() {
        return false;
    }

    public int getMax(int paramIndex) {
        return Integer.MAX_VALUE;
    }

    public int getMin(int paramIndex) {
        return 0;
    }

    public int get(int paramIndex) {
        return paramIndex == 0 ? param0 : paramIndex == 1 ? param1 : param2;
    }

    public BlockPos getOffset(Vec3d ray, Direction face, PlacementOptions option) {
        return BlockPos.ORIGIN;
    }

    public abstract Collection<BlockPos> addToGlobalPositions(
        WorldAccess world,
        BlockPos targetPos,
        Direction targetFace,
        Collection<BlockPos> affectedPositions,
        TerrainTools usedTool
    );

}

package com.zurrtum.create.content.equipment.zapper.terrainzapper;

import com.zurrtum.create.infrastructure.component.TerrainTools;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

import java.util.Collection;
import java.util.List;

public abstract class ShapedBrush extends Brush {

    public ShapedBrush(int amtParams) {
        super(amtParams);
    }

    @Override
    public Collection<BlockPos> addToGlobalPositions(
        WorldAccess world,
        BlockPos targetPos,
        Direction targetFace,
        Collection<BlockPos> affectedPositions,
        TerrainTools usedTool
    ) {
        List<BlockPos> includedPositions = getIncludedPositions();
        if (includedPositions == null)
            return affectedPositions;
        for (BlockPos blockPos : includedPositions)
            affectedPositions.add(targetPos.add(blockPos));
        return affectedPositions;
    }

    abstract List<BlockPos> getIncludedPositions();

}

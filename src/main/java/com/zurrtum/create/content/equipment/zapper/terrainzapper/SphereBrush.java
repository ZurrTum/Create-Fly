package com.zurrtum.create.content.equipment.zapper.terrainzapper;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.infrastructure.component.PlacementOptions;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SphereBrush extends ShapedBrush {

    public static final int MAX_RADIUS = 10;
    private final Map<Integer, List<BlockPos>> cachedBrushes;

    public SphereBrush() {
        super(1);

        cachedBrushes = new HashMap<>();
        for (int i = 0; i <= MAX_RADIUS; i++) {
            int radius = i;
            List<BlockPos> positions = BlockPos.stream(BlockPos.ORIGIN.add(-i - 1, -i - 1, -i - 1), BlockPos.ORIGIN.add(i + 1, i + 1, i + 1))
                .map(BlockPos::new).filter(p -> VecHelper.getCenterOf(p).distanceTo(VecHelper.getCenterOf(BlockPos.ZERO)) < radius + .5f)
                .collect(Collectors.toList());
            cachedBrushes.put(i, positions);
        }
    }

    @Override
    public BlockPos getOffset(Vec3d ray, Direction face, PlacementOptions option) {
        if (option == PlacementOptions.Merged)
            return BlockPos.ORIGIN;

        int offset = option == PlacementOptions.Attached ? 0 : -1;
        int r = (param0 + 1 + offset);

        return BlockPos.ORIGIN.offset(face, r * (option == PlacementOptions.Attached ? 1 : -1));
    }

    @Override
    public int getMax(int paramIndex) {
        return MAX_RADIUS;
    }

    @Override
    List<BlockPos> getIncludedPositions() {
        return cachedBrushes.get(param0);
    }

}

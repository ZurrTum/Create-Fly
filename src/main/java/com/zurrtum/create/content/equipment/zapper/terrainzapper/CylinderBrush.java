package com.zurrtum.create.content.equipment.zapper.terrainzapper;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.infrastructure.component.PlacementOptions;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CylinderBrush extends ShapedBrush {

    public static final int MAX_RADIUS = 8;
    public static final int MAX_HEIGHT = 8;
    private final Map<Pair<Integer, Integer>, List<BlockPos>> cachedBrushes;

    public CylinderBrush() {
        super(2);

        cachedBrushes = new HashMap<>();
        for (int i = 0; i <= MAX_RADIUS; i++) {
            int radius = i;
            List<BlockPos> positions = BlockPos.stream(BlockPos.ORIGIN.add(-i - 1, 0, -i - 1), BlockPos.ORIGIN.add(i + 1, 0, i + 1))
                .map(BlockPos::new).filter(p -> VecHelper.getCenterOf(p).distanceTo(VecHelper.getCenterOf(BlockPos.ZERO)) < radius + .42f).toList();
            for (int h = 0; h <= MAX_HEIGHT; h++) {
                List<BlockPos> stackedPositions = new ArrayList<>();
                for (int layer = 0; layer < h; layer++) {
                    int yOffset = layer - h / 2;
                    for (BlockPos p : positions)
                        stackedPositions.add(p.up(yOffset));
                }
                cachedBrushes.put(Pair.of(i, h), stackedPositions);
            }
        }
    }

    @Override
    public BlockPos getOffset(Vec3d ray, Direction face, PlacementOptions option) {
        if (option == PlacementOptions.Merged)
            return BlockPos.ORIGIN;

        int offset = option == PlacementOptions.Attached ? 0 : -1;
        boolean negative = face.getDirection() == AxisDirection.NEGATIVE;
        int yOffset = option == PlacementOptions.Attached ? negative ? 1 : 2 : negative ? 0 : -1;
        int r = (param0 + 1 + offset);
        int y = (param1 + (param1 == 0 ? 0 : yOffset)) / 2;

        return BlockPos.ORIGIN.offset(face, (face.getAxis().isVertical() ? y : r) * (option == PlacementOptions.Attached ? 1 : -1));
    }

    @Override
    public int getMax(int paramIndex) {
        return paramIndex == 0 ? MAX_RADIUS : MAX_HEIGHT;
    }

    @Override
    public int getMin(int paramIndex) {
        return paramIndex == 0 ? 0 : 1;
    }

    @Override
    public List<BlockPos> getIncludedPositions() {
        return cachedBrushes.get(Pair.of(param0, param1));
    }

}

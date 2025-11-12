package com.zurrtum.create.content.equipment.symmetryWand.mirror;

import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.infrastructure.component.SymmetryMirror;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TriplePlaneMirror extends SymmetryMirror {

    public TriplePlaneMirror(Vec3 pos) {
        super(pos);
        orientationIndex = 0;
    }

    @Override
    public Map<BlockPos, Pair<Direction, BlockState>> process(BlockPos position, Pair<Direction, BlockState> pair) {
        Map<BlockPos, Pair<Direction, BlockState>> result = new HashMap<>();

        Direction side = pair.getFirst();
        BlockState block = pair.getSecond();
        result.put(flipX(position), Pair.of(flipX(side), flipX(block)));
        result.put(flipZ(position), Pair.of(flipZ(side), flipZ(block)));
        result.put(flipX(flipZ(position)), Pair.of(flipXZ(side), flipX(flipZ(block))));

        result.put(flipD1(position), Pair.of(flipD1(side), flipD1(block)));
        result.put(flipD1(flipX(position)), Pair.of(flipD1X(side), flipD1(flipX(block))));
        result.put(flipD1(flipZ(position)), Pair.of(flipD1Z(side), flipD1(flipZ(block))));
        result.put(flipD1(flipX(flipZ(position))), Pair.of(flipD1XZ(side), flipD1(flipX(flipZ(block)))));

        return result;
    }

    @Override
    public Set<BlockPos> process(BlockPos position) {
        Set<BlockPos> positions = new HashSet<>();
        positions.add(flipX(position));
        positions.add(flipZ(position));
        positions.add(flipX(flipZ(position)));

        positions.add(flipD1(position));
        positions.add(flipD1(flipX(position)));
        positions.add(flipD1(flipZ(position)));
        positions.add(flipD1(flipX(flipZ(position))));
        return positions;
    }

    @Override
    public String typeName() {
        return TRIPLE_PLANE;
    }

    @Override
    protected void setOrientation() {
    }

    @Override
    public void setOrientation(int index) {
    }

    @Override
    public StringRepresentable getOrientation() {
        return CrossPlaneMirror.Align.Y;
    }
}

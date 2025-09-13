package com.zurrtum.create.content.equipment.symmetryWand.mirror;

import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.infrastructure.component.SymmetryMirror;
import net.minecraft.block.BlockState;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CrossPlaneMirror extends SymmetryMirror {
    public enum Align implements StringIdentifiable {
        Y("y"),
        D("d");

        private final String name;

        private Align(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public CrossPlaneMirror(Vec3d pos) {
        super(pos);
        orientation = Align.Y;
    }

    @Override
    protected void setOrientation() {
        if (orientationIndex < 0)
            orientationIndex += Align.values().length;
        if (orientationIndex >= Align.values().length)
            orientationIndex -= Align.values().length;
        orientation = Align.values()[orientationIndex];
    }

    @Override
    public void setOrientation(int index) {
        this.orientation = Align.values()[index];
        orientationIndex = index;
    }

    @Override
    public Map<BlockPos, Pair<Direction, BlockState>> process(BlockPos position, Pair<Direction, BlockState> pair) {
        Map<BlockPos, Pair<Direction, BlockState>> result = new HashMap<>();

        Direction side = pair.getFirst();
        BlockState block = pair.getSecond();
        switch ((Align) orientation) {
            case D:
                result.put(flipD1(position), Pair.of(flipD1(side), flipD1(block)));
                result.put(flipD2(position), Pair.of(flipD2(side), flipD2(block)));
                result.put(flipD1(flipD2(position)), Pair.of(flipD1D2(side), flipD1(flipD2(block))));
                break;
            case Y:
                result.put(flipX(position), Pair.of(flipX(side), flipX(block)));
                result.put(flipZ(position), Pair.of(flipZ(side), flipZ(block)));
                result.put(flipX(flipZ(position)), Pair.of(flipXZ(side), flipX(flipZ(block))));
                break;
            default:
                break;
        }

        return result;
    }

    @Override
    public Set<BlockPos> process(BlockPos position) {
        Set<BlockPos> positions = new HashSet<>();
        switch ((Align) orientation) {
            case D:
                positions.add(flipD1(position));
                positions.add(flipD2(position));
                positions.add(flipD1(flipD2(position)));
                break;
            case Y:
                positions.add(flipX(position));
                positions.add(flipZ(position));
                positions.add(flipX(flipZ(position)));
                break;
            default:
                break;
        }
        return positions;
    }

    @Override
    public String typeName() {
        return CROSS_PLANE;
    }
}

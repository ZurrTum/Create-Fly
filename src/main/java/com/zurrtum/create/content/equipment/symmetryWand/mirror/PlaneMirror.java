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

public class PlaneMirror extends SymmetryMirror {

    public enum Align implements StringIdentifiable {
        XY("xy"),
        YZ("yz");

        private final String name;

        Align(String name) {
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

    public PlaneMirror(Vec3d pos) {
        super(pos);
        orientation = Align.XY;
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
            case XY:
                result.put(flipZ(position), Pair.of(flipZ(side), flipZ(block)));
                break;
            case YZ:
                result.put(flipX(position), Pair.of(flipX(side), flipX(block)));
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
            case XY:
                positions.add(flipZ(position));
                break;
            case YZ:
                positions.add(flipX(position));
                break;
            default:
                break;
        }
        return positions;
    }

    @Override
    public String typeName() {
        return PLANE;
    }
}

package com.zurrtum.create.content.equipment.symmetryWand.mirror;

import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.infrastructure.component.SymmetryMirror;
import net.minecraft.block.BlockState;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.Set;

public class EmptyMirror extends SymmetryMirror {

    public enum Align implements StringIdentifiable {
        None("none");

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

    public EmptyMirror(Vec3d pos) {
        super(pos);
        orientation = Align.None;
    }

    @Override
    protected void setOrientation() {
    }

    @Override
    public void setOrientation(int index) {
        this.orientation = Align.values()[index];
        orientationIndex = index;
    }

    @Override
    public Map<BlockPos, Pair<Direction, BlockState>> process(BlockPos position, Pair<Direction, BlockState> block) {
        return Map.of();
    }

    @Override
    public Set<BlockPos> process(BlockPos position) {
        return Set.of();
    }

    @Override
    public String typeName() {
        return EMPTY;
    }
}

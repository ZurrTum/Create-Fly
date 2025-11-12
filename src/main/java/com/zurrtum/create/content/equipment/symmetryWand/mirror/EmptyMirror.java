package com.zurrtum.create.content.equipment.symmetryWand.mirror;

import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.infrastructure.component.SymmetryMirror;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class EmptyMirror extends SymmetryMirror {

    public enum Align implements StringRepresentable {
        None("none");

        private final String name;

        Align(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public EmptyMirror(Vec3 pos) {
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

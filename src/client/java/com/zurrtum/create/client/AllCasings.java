package com.zurrtum.create.client;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import com.zurrtum.create.content.fluids.pipes.EncasedPipeBlock;
import com.zurrtum.create.content.kinetics.gearbox.GearboxBlock;
import com.zurrtum.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import com.zurrtum.create.content.kinetics.simpleRelays.encased.EncasedShaftBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiPredicate;

public class AllCasings {
    private static final Map<Block, Entry> ALL = new IdentityHashMap<>();

    public static Entry get(BlockState state) {
        return ALL.get(state.getBlock());
    }

    public static Entry make(Block block, CTSpriteShiftEntry casing) {
        return make(block, casing, (s, f) -> true);
    }

    public static Entry make(Block block, CTSpriteShiftEntry casing, BiPredicate<BlockState, Direction> predicate) {
        Entry entry = new Entry(casing, predicate);
        ALL.put(block, entry);
        return entry;
    }

    public static class Entry {
        private final CTSpriteShiftEntry casing;
        private final BiPredicate<BlockState, Direction> predicate;

        private Entry(CTSpriteShiftEntry casing, BiPredicate<BlockState, Direction> predicate) {
            this.casing = casing;
            this.predicate = predicate;
        }

        public CTSpriteShiftEntry getCasing() {
            return casing;
        }

        public boolean isSideValid(BlockState state, Direction face) {
            return predicate.test(state, face);
        }
    }

    public static void register() {
        make(AllBlocks.ANDESITE_CASING, AllSpriteShifts.ANDESITE_CASING);
        make(AllBlocks.BRASS_CASING, AllSpriteShifts.BRASS_CASING);
        make(AllBlocks.GEARBOX, AllSpriteShifts.ANDESITE_CASING, (s, f) -> f.getAxis() == s.get(GearboxBlock.AXIS));
        make(AllBlocks.ANDESITE_ENCASED_SHAFT, AllSpriteShifts.ANDESITE_CASING, (s, f) -> f.getAxis() != s.get(EncasedShaftBlock.AXIS));
        make(AllBlocks.BRASS_ENCASED_SHAFT, AllSpriteShifts.BRASS_CASING, (s, f) -> f.getAxis() != s.get(EncasedShaftBlock.AXIS));
        make(
            AllBlocks.ANDESITE_ENCASED_COGWHEEL,
            AllSpriteShifts.ANDESITE_CASING,
            (s, f) -> f.getAxis() == s.get(EncasedCogwheelBlock.AXIS) && !s.get(f.getDirection() == AxisDirection.POSITIVE ? EncasedCogwheelBlock.TOP_SHAFT : EncasedCogwheelBlock.BOTTOM_SHAFT)
        );
        make(
            AllBlocks.BRASS_ENCASED_COGWHEEL,
            AllSpriteShifts.BRASS_CASING,
            (s, f) -> f.getAxis() == s.get(EncasedCogwheelBlock.AXIS) && !s.get(f.getDirection() == AxisDirection.POSITIVE ? EncasedCogwheelBlock.TOP_SHAFT : EncasedCogwheelBlock.BOTTOM_SHAFT)
        );
        make(
            AllBlocks.ANDESITE_ENCASED_LARGE_COGWHEEL,
            AllSpriteShifts.ANDESITE_CASING,
            (s, f) -> f.getAxis() == s.get(EncasedCogwheelBlock.AXIS) && !s.get(f.getDirection() == AxisDirection.POSITIVE ? EncasedCogwheelBlock.TOP_SHAFT : EncasedCogwheelBlock.BOTTOM_SHAFT)
        );
        make(
            AllBlocks.BRASS_ENCASED_LARGE_COGWHEEL,
            AllSpriteShifts.BRASS_CASING,
            (s, f) -> f.getAxis() == s.get(EncasedCogwheelBlock.AXIS) && !s.get(f.getDirection() == AxisDirection.POSITIVE ? EncasedCogwheelBlock.TOP_SHAFT : EncasedCogwheelBlock.BOTTOM_SHAFT)
        );
        make(AllBlocks.ENCASED_FLUID_PIPE, AllSpriteShifts.COPPER_CASING, (s, f) -> !s.get(EncasedPipeBlock.FACING_TO_PROPERTY_MAP.get(f)));
    }
}

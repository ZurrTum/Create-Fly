package com.zurrtum.create.content.fluids;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllFluids;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AllFlowCollision {
    public static final Map<FlowEntry, BlockState> Flow = new HashMap<>();
    public static final Map<SpillEntry, BlockState> Spill = new HashMap<>();

    public static void register() {
        Flow.put(new FlowEntry(Fluids.WATER, Fluids.LAVA), Blocks.COBBLESTONE.getDefaultState());
        Flow.put(new FlowEntry(Fluids.LAVA, AllFluids.HONEY), AllBlocks.LIMESTONE.getDefaultState());
        Flow.put(new FlowEntry(Fluids.LAVA, AllFluids.CHOCOLATE), AllBlocks.SCORIA.getDefaultState());
        Spill.put(new SpillEntry(Fluids.LAVA, Fluids.WATER), Blocks.OBSIDIAN.getDefaultState());
        Spill.put(new SpillEntry(Fluids.FLOWING_LAVA, Fluids.WATER), Blocks.COBBLESTONE.getDefaultState());
        Spill.put(new SpillEntry(Fluids.WATER, Fluids.LAVA), Blocks.STONE.getDefaultState());
        Spill.put(new SpillEntry(Fluids.FLOWING_WATER, Fluids.LAVA), Blocks.COBBLESTONE.getDefaultState());
        Spill.put(new SpillEntry(AllFluids.HONEY, Fluids.LAVA), AllBlocks.LIMESTONE.getDefaultState());
        Spill.put(new SpillEntry(AllFluids.CHOCOLATE, Fluids.LAVA), AllBlocks.SCORIA.getDefaultState());
        Spill.put(new SpillEntry(Fluids.FLOWING_LAVA, AllFluids.HONEY), AllBlocks.LIMESTONE.getDefaultState());
        Spill.put(new SpillEntry(Fluids.FLOWING_LAVA, AllFluids.CHOCOLATE), AllBlocks.SCORIA.getDefaultState());
    }

    private static abstract class Entry {
        private final Fluid a;
        private final Fluid b;

        public Entry(Fluid a, Fluid b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Entry entry) {
                return entry.a == a && entry.b == b;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(a, b);
        }
    }

    public static class SpillEntry extends Entry {
        public SpillEntry(Fluid worldFluid, Fluid pipeFluid) {
            super(worldFluid, pipeFluid);
        }
    }

    public static class FlowEntry extends Entry {
        public FlowEntry(Fluid firstFluid, Fluid secondFluid) {
            this(firstFluid, secondFluid, Registries.FLUID.getRawId(firstFluid) > Registries.FLUID.getRawId(secondFluid));
        }

        private FlowEntry(Fluid firstFluid, Fluid secondFluid, boolean reverse) {
            super(reverse ? secondFluid : firstFluid, reverse ? firstFluid : secondFluid);
        }
    }
}

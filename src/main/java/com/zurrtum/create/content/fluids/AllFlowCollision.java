package com.zurrtum.create.content.fluids;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllFluids;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class AllFlowCollision {
    public static final Map<FlowEntry, BlockState> Flow = new HashMap<>();
    public static final Map<SpillEntry, BlockState> Spill = new HashMap<>();

    public static void register() {
        Flow.put(new FlowEntry(Fluids.WATER, Fluids.LAVA), Blocks.COBBLESTONE.defaultBlockState());
        Flow.put(new FlowEntry(Fluids.LAVA, AllFluids.HONEY), AllBlocks.LIMESTONE.defaultBlockState());
        Flow.put(new FlowEntry(Fluids.LAVA, AllFluids.CHOCOLATE), AllBlocks.SCORIA.defaultBlockState());
        Spill.put(new SpillEntry(Fluids.LAVA, Fluids.WATER), Blocks.OBSIDIAN.defaultBlockState());
        Spill.put(new SpillEntry(Fluids.FLOWING_LAVA, Fluids.WATER), Blocks.COBBLESTONE.defaultBlockState());
        Spill.put(new SpillEntry(Fluids.WATER, Fluids.LAVA), Blocks.STONE.defaultBlockState());
        Spill.put(new SpillEntry(Fluids.FLOWING_WATER, Fluids.LAVA), Blocks.COBBLESTONE.defaultBlockState());
        Spill.put(new SpillEntry(AllFluids.HONEY, Fluids.LAVA), AllBlocks.LIMESTONE.defaultBlockState());
        Spill.put(new SpillEntry(AllFluids.CHOCOLATE, Fluids.LAVA), AllBlocks.SCORIA.defaultBlockState());
        Spill.put(new SpillEntry(Fluids.FLOWING_LAVA, AllFluids.HONEY), AllBlocks.LIMESTONE.defaultBlockState());
        Spill.put(new SpillEntry(Fluids.FLOWING_LAVA, AllFluids.CHOCOLATE), AllBlocks.SCORIA.defaultBlockState());
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
            this(firstFluid, secondFluid, BuiltInRegistries.FLUID.getId(firstFluid) > BuiltInRegistries.FLUID.getId(secondFluid));
        }

        private FlowEntry(Fluid firstFluid, Fluid secondFluid, boolean reverse) {
            super(reverse ? secondFluid : firstFluid, reverse ? firstFluid : secondFluid);
        }
    }
}

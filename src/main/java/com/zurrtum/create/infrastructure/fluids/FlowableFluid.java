package com.zurrtum.create.infrastructure.fluids;

import net.minecraft.block.*;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public abstract class FlowableFluid extends WaterFluid {
    private final FluidEntry entry;

    FlowableFluid(FluidEntry entry) {
        this.entry = entry;
    }

    public FluidEntry getEntry() {
        return entry;
    }

    @Override
    public Fluid getFlowing() {
        return entry.flowing;
    }

    @Override
    public Fluid getStill() {
        return entry.still;
    }

    @Override
    public Item getBucketItem() {
        if (entry.bucket == null) {
            return Items.AIR;
        }
        return entry.bucket;
    }

    @Override
    public BlockState toBlockState(FluidState state) {
        if (entry.block == null) {
            return Blocks.AIR.getDefaultState();
        }
        return entry.block.getDefaultState().with(FluidBlock.LEVEL, getBlockStateLevel(state));
    }

    @Override
    public void randomDisplayTick(World world, BlockPos pos, FluidState state, Random random) {
    }

    @Override
    @Nullable
    public ParticleEffect getParticle() {
        return null;
    }

    @Override
    public boolean matchesType(Fluid fluid) {
        return fluid == entry.still || fluid == entry.flowing;
    }

    public static class Flowing extends FlowableFluid {
        public Flowing(FluidEntry entry) {
            super(entry);
        }

        @Override
        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getLevel(FluidState state) {
            return state.get(LEVEL);
        }

        @Override
        public boolean isStill(FluidState state) {
            return false;
        }
    }

    public static class Still extends FlowableFluid {
        public Still(FluidEntry entry) {
            super(entry);
        }

        @Override
        public int getLevel(FluidState state) {
            return 8;
        }

        @Override
        public boolean isStill(FluidState state) {
            return true;
        }
    }

    @Override
    protected void flow(WorldAccess world, BlockPos pos, BlockState state, Direction direction, FluidState fluidState) {
        if (state.getBlock() instanceof FluidFillable fluidFillable) {
            fluidFillable.tryFillWithFluid(world, pos, state, fluidState);
        } else if (state.getFluidState().isEmpty()) {
            world.setBlockState(pos, fluidState.getBlockState(), Block.NOTIFY_ALL);
        }
    }

    @Override
    public int getLevelDecreasePerBlock(WorldView world) {
        return 2;
    }

    @Override
    public int getMaxFlowDistance(WorldView world) {
        return 3;
    }

    @Override
    public int getTickRate(WorldView world) {
        return 25;
    }

    @Override
    protected boolean isInfinite(ServerWorld world) {
        return false;
    }

    @Override
    public boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
        return false;
    }
}

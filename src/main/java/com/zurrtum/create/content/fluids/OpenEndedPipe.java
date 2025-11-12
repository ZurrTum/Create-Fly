package com.zurrtum.create.content.fluids;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllFluids;
import com.zurrtum.create.api.effect.OpenPipeEffectHandler;
import com.zurrtum.create.catnip.math.BlockFace;
import com.zurrtum.create.content.fluids.pipes.VanillaFluidTargets;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import com.zurrtum.create.infrastructure.fluids.SidedFluidInventory;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public class OpenEndedPipe extends FlowSource {
    private static final Function<BlockPos, Codec<OpenEndedPipe>> CODEC = Util.memoize(pos -> RecordCodecBuilder.create(instance -> instance.group(
        FluidStack.OPTIONAL_CODEC.fieldOf("Fluid").forGetter(i -> i.fluidHandler.stack),
        Codec.BOOL.fieldOf("Pulling").forGetter(i -> i.wasPulling),
        Direction.CODEC.fieldOf("Direction").forGetter(i -> i.location.getFace())
    ).apply(instance, (stack, wasPulling, direction) -> new OpenEndedPipe(stack, wasPulling, pos, direction))));

    public static Codec<OpenEndedPipe> codec(BlockPos pos) {
        return CODEC.apply(pos);
    }

    private Level world;
    private final BlockPos pos;
    private AABB aoe;

    private final OpenEndFluidHandler fluidHandler;
    private final BlockPos outputPos;
    private boolean wasPulling;

    public OpenEndedPipe(BlockFace face) {
        super(face);
        fluidHandler = new OpenEndFluidHandler();
        outputPos = face.getConnectedPos();
        pos = face.getPos();
        aoe = new AABB(outputPos).expandTowards(0, -1, 0);
        if (face.getFace() == Direction.DOWN)
            aoe = aoe.expandTowards(0, -1, 0);
    }

    private OpenEndedPipe(FluidStack stack, boolean wasPulling, BlockPos pos, Direction direction) {
        this(new BlockFace(pos, direction));
        this.fluidHandler.stack = stack;
        this.wasPulling = wasPulling;
    }

    public Level getWorld() {
        return world;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockPos getOutputPos() {
        return outputPos;
    }

    public AABB getAOE() {
        return aoe;
    }

    @Override
    public void manageSource(Level world, BlockEntity networkBE) {
        this.world = world;
    }

    @Override
    @Nullable
    public FluidInventory provideHandler() {
        return fluidHandler;
    }

    @Override
    public boolean isEndpoint() {
        return true;
    }

    private FluidStack removeFluidFromSpace(boolean simulate) {
        if (world == null)
            return FluidStack.EMPTY;
        if (!world.isLoaded(outputPos))
            return FluidStack.EMPTY;

        BlockState state = world.getBlockState(outputPos);
        FluidState fluidState = state.getFluidState();
        boolean waterlog = state.hasProperty(WATERLOGGED);

        FluidStack drainBlock = VanillaFluidTargets.drainBlock(world, outputPos, state, simulate);
        if (!drainBlock.isEmpty()) {
            if (!simulate && state.hasProperty(BlockStateProperties.LEVEL_HONEY) && drainBlock.getFluid() == AllFluids.HONEY)
                AdvancementBehaviour.tryAward(world, pos, AllAdvancements.HONEY_DRAIN);
            return drainBlock;
        }

        if (!waterlog && !state.canBeReplaced())
            return FluidStack.EMPTY;
        if (fluidState.isEmpty() || !fluidState.isSource())
            return FluidStack.EMPTY;

        FluidStack stack = new FluidStack(fluidState.getType(), BucketFluidInventory.CAPACITY);

        if (simulate)
            return stack;

        if (FluidHelper.isWater(stack.getFluid()))
            AdvancementBehaviour.tryAward(world, pos, AllAdvancements.WATER_SUPPLY);

        if (waterlog) {
            world.setBlock(outputPos, state.setValue(WATERLOGGED, false), Block.UPDATE_ALL);
            world.scheduleTick(outputPos, Fluids.WATER, 1);
        } else {
            var newState = fluidState.createLegacyBlock().setValue(LiquidBlock.LEVEL, 14);

            var newFluidState = newState.getFluidState();

            if (newFluidState.getType() instanceof FlowingFluid flowing && world instanceof ServerLevel serverWorld) {
                var potentiallyFilled = flowing.getNewLiquid(serverWorld, outputPos, newState);

                // Check if we'd immediately become the same fluid again.
                if (potentiallyFilled.equals(fluidState)) {
                    // If so, no need to update the block state.
                    return stack;
                }
            }

            world.setBlock(outputPos, newState, Block.UPDATE_ALL);
        }

        return stack;
    }

    private boolean provideFluidToSpace(FluidStack fluid, boolean simulate) {
        if (world == null)
            return false;
        if (!world.isLoaded(outputPos))
            return false;

        BlockState state = world.getBlockState(outputPos);
        FluidState fluidState = state.getFluidState();
        boolean waterlog = state.hasProperty(WATERLOGGED);

        if (!waterlog && !state.canBeReplaced())
            return false;
        if (fluid.isEmpty())
            return false;
        if (!(fluid.getFluid() instanceof FlowingFluid))
            return false;
        if (!FluidHelper.hasBlockState(fluid.getFluid()))
            return true;

        if (!fluidState.isEmpty() && FluidHelper.convertToStill(fluidState.getType()) != fluid.getFluid()) {
            FluidReactions.handlePipeSpillCollision(world, outputPos, fluid.getFluid(), fluidState);
            return false;
        }

        if (fluidState.isSource())
            return false;
        if (waterlog && fluid.getFluid() != Fluids.WATER)
            return false;
        if (simulate)
            return true;

        if (!AllConfigs.server().fluids.pipesPlaceFluidSourceBlocks.get())
            return true;

        if (world.dimensionType().ultraWarm() && FluidHelper.isTag(fluid, FluidTags.WATER)) {
            int i = outputPos.getX();
            int j = outputPos.getY();
            int k = outputPos.getZ();
            world.playSound(
                null,
                i,
                j,
                k,
                SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS,
                0.5F,
                2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F
            );
            return true;
        }

        if (waterlog) {
            world.setBlock(outputPos, state.setValue(WATERLOGGED, true), Block.UPDATE_ALL);
            world.scheduleTick(outputPos, Fluids.WATER, 1);
            return true;
        }

        world.setBlock(outputPos, fluid.getFluid().defaultFluidState().createLegacyBlock(), Block.UPDATE_ALL);
        return true;
    }

    private class OpenEndFluidHandler implements SidedFluidInventory {
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private static final Optional<Integer> MAX = Optional.of(BucketFluidInventory.CAPACITY);
        private static final int[] SLOTS = {0, 1};
        private FluidStack stack = FluidStack.EMPTY;
        private int previousAmount = 0;

        @Override
        public FluidStack onExtract(FluidStack stack) {
            return removeMaxSize(stack, MAX);
        }

        @Override
        public int getMaxAmountPerStack() {
            return BucketFluidInventory.CAPACITY;
        }

        @Override
        public int size() {
            return 2;
        }

        @Override
        public int[] getAvailableSlots(Direction side) {
            return SLOTS;
        }

        @Override
        public FluidStack getStack(int slot) {
            if (slot > 1) {
                return FluidStack.EMPTY;
            }
            if (slot == 0) {
                return stack;
            }
            if (stack == FluidStack.EMPTY) {
                stack = removeFluidFromSpace(false);
                setMaxSize(stack, MAX);
                return stack;
            }
            return FluidStack.EMPTY;
        }

        @Override
        public int getMaxAmount(FluidStack stack) {
            OpenPipeEffectHandler effectHandler = OpenPipeEffectHandler.REGISTRY.get(stack.getFluid());
            if (effectHandler != null && !FluidHelper.hasBlockState(stack.getFluid())) {
                return 81;
            }
            return SidedFluidInventory.super.getMaxAmount(stack);
        }

        @Override
        public boolean canInsert(int slot, FluidStack resource, @Nullable Direction dir) {
            if (slot != 0 || !provideFluidToSpace(resource, true))
                return false;
            if (!stack.isEmpty() && !matches(stack, resource))
                stack = FluidStack.EMPTY;
            return true;
        }

        @Override
        public boolean canExtract(int slot, FluidStack stack, Direction dir) {
            return world != null && world.isLoaded(outputPos);
        }

        @Override
        public void setStack(int slot, FluidStack stack) {
            if (slot != 0) {
                return;
            }
            if (stack != FluidStack.EMPTY) {
                setMaxSize(stack, MAX);
            }
            this.stack = stack;
        }

        @Override
        public void markDirty() {
            int amount = stack.getAmount();
            if (amount > previousAmount) {
                Fluid fluid = stack.getFluid();
                OpenPipeEffectHandler effectHandler = OpenPipeEffectHandler.REGISTRY.get(fluid);
                if (effectHandler != null) {
                    effectHandler.apply(world, aoe, stack);
                }
                if (stack.getAmount() == BucketFluidInventory.CAPACITY || !FluidHelper.hasBlockState(fluid)) {
                    if (provideFluidToSpace(stack, false)) {
                        stack = FluidStack.EMPTY;
                    }
                }
                wasPulling = false;
            } else if (amount < previousAmount) {
                wasPulling = true;
            }
            previousAmount = stack.getAmount();
        }
    }
}

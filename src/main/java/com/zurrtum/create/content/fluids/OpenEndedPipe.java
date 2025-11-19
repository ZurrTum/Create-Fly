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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

import static net.minecraft.state.property.Properties.WATERLOGGED;

public class OpenEndedPipe extends FlowSource {
    private static final Function<BlockPos, Codec<OpenEndedPipe>> CODEC = Util.memoize(pos -> RecordCodecBuilder.create(instance -> instance.group(
        FluidStack.OPTIONAL_CODEC.fieldOf("Fluid").forGetter(i -> i.fluidHandler.stack),
        Codec.BOOL.fieldOf("Pulling").forGetter(i -> i.wasPulling),
        Direction.CODEC.fieldOf("Direction").forGetter(i -> i.location.getFace())
    ).apply(instance, (stack, wasPulling, direction) -> new OpenEndedPipe(stack, wasPulling, pos, direction))));

    public static Codec<OpenEndedPipe> codec(BlockPos pos) {
        return CODEC.apply(pos);
    }

    private World world;
    private final BlockPos pos;
    private Box aoe;

    private final OpenEndFluidHandler fluidHandler;
    private final BlockPos outputPos;
    private boolean wasPulling;

    public OpenEndedPipe(BlockFace face) {
        super(face);
        fluidHandler = new OpenEndFluidHandler();
        outputPos = face.getConnectedPos();
        pos = face.getPos();
        aoe = new Box(outputPos).stretch(0, -1, 0);
        if (face.getFace() == Direction.DOWN)
            aoe = aoe.stretch(0, -1, 0);
    }

    private OpenEndedPipe(FluidStack stack, boolean wasPulling, BlockPos pos, Direction direction) {
        this(new BlockFace(pos, direction));
        this.fluidHandler.stack = stack;
        this.wasPulling = wasPulling;
    }

    public World getWorld() {
        return world;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockPos getOutputPos() {
        return outputPos;
    }

    public Box getAOE() {
        return aoe;
    }

    @Override
    public void manageSource(World world, BlockEntity networkBE) {
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
        if (!world.isPosLoaded(outputPos))
            return FluidStack.EMPTY;

        BlockState state = world.getBlockState(outputPos);
        FluidState fluidState = state.getFluidState();
        boolean waterlog = state.contains(WATERLOGGED);

        FluidStack drainBlock = VanillaFluidTargets.drainBlock(world, outputPos, state, simulate);
        if (!drainBlock.isEmpty()) {
            if (!simulate && state.contains(Properties.HONEY_LEVEL) && drainBlock.getFluid() == AllFluids.HONEY)
                AdvancementBehaviour.tryAward(world, pos, AllAdvancements.HONEY_DRAIN);
            return drainBlock;
        }

        if (!waterlog && !state.isReplaceable())
            return FluidStack.EMPTY;
        if (fluidState.isEmpty() || !fluidState.isStill())
            return FluidStack.EMPTY;

        FluidStack stack = new FluidStack(fluidState.getFluid(), BucketFluidInventory.CAPACITY);

        if (simulate)
            return stack;

        if (FluidHelper.isWater(stack.getFluid()))
            AdvancementBehaviour.tryAward(world, pos, AllAdvancements.WATER_SUPPLY);

        if (waterlog) {
            world.setBlockState(outputPos, state.with(WATERLOGGED, false), Block.NOTIFY_ALL);
            world.scheduleFluidTick(outputPos, Fluids.WATER, 1);
        } else {
            var newState = fluidState.getBlockState().with(FluidBlock.LEVEL, 14);

            var newFluidState = newState.getFluidState();

            if (newFluidState.getFluid() instanceof FlowableFluid flowing && world instanceof ServerWorld serverWorld) {
                var potentiallyFilled = flowing.getUpdatedState(serverWorld, outputPos, newState);

                // Check if we'd immediately become the same fluid again.
                if (potentiallyFilled.equals(fluidState)) {
                    // If so, no need to update the block state.
                    return stack;
                }
            }

            world.setBlockState(outputPos, newState, Block.NOTIFY_ALL);
        }

        return stack;
    }

    private boolean provideFluidToSpace(FluidStack fluid, boolean simulate) {
        if (world == null)
            return false;
        if (!world.isPosLoaded(outputPos))
            return false;

        BlockState state = world.getBlockState(outputPos);
        FluidState fluidState = state.getFluidState();
        boolean waterlog = state.contains(WATERLOGGED);

        if (!waterlog && !state.isReplaceable())
            return false;
        if (fluid.isEmpty())
            return false;
        if (!(fluid.getFluid() instanceof FlowableFluid))
            return false;
        if (!FluidHelper.hasBlockState(fluid.getFluid()))
            return true;

        if (!fluidState.isEmpty() && FluidHelper.convertToStill(fluidState.getFluid()) != fluid.getFluid()) {
            FluidReactions.handlePipeSpillCollision(world, outputPos, fluid.getFluid(), fluidState);
            return false;
        }

        if (fluidState.isStill())
            return false;
        if (waterlog && fluid.getFluid() != Fluids.WATER)
            return false;
        if (simulate)
            return true;

        if (!AllConfigs.server().fluids.pipesPlaceFluidSourceBlocks.get())
            return true;

        if (world.getDimension().ultrawarm() && FluidHelper.isTag(fluid, FluidTags.WATER)) {
            int i = outputPos.getX();
            int j = outputPos.getY();
            int k = outputPos.getZ();
            world.playSound(
                null,
                i,
                j,
                k,
                SoundEvents.BLOCK_FIRE_EXTINGUISH,
                SoundCategory.BLOCKS,
                0.5F,
                2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F
            );
            return true;
        }

        if (waterlog) {
            world.setBlockState(outputPos, state.with(WATERLOGGED, true), Block.NOTIFY_ALL);
            world.scheduleFluidTick(outputPos, Fluids.WATER, 1);
            return true;
        }

        world.setBlockState(outputPos, fluid.getFluid().getDefaultState().getBlockState(), Block.NOTIFY_ALL);
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
                if (!stack.isEmpty()) {
                    setMaxSize(stack, MAX);
                }
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
            return world != null && world.isPosLoaded(outputPos);
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

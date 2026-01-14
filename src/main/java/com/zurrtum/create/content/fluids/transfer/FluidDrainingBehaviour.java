package com.zurrtum.create.content.fluids.transfer;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.BBHelper;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;


public class FluidDrainingBehaviour extends FluidManipulationBehaviour {

    public static final BehaviourType<FluidDrainingBehaviour> TYPE = new BehaviourType<>();

    Fluid fluid;

    // Execution
    Set<BlockPos> validationSet;
    PriorityQueue<BlockPosEntry> queue;
    boolean isValid;

    // Validation
    List<BlockPosEntry> validationFrontier;
    Set<BlockPos> validationVisited;
    Set<BlockPos> newValidationSet;

    public FluidDrainingBehaviour(SmartBlockEntity be) {
        super(be);
        validationVisited = new HashSet<>();
        validationFrontier = new ArrayList<>();
        validationSet = new HashSet<>();
        newValidationSet = new HashSet<>();
        queue = new ObjectHeapPriorityQueue<>(this::comparePositions);
    }

    public boolean pullNext(BlockPos root, boolean simulate) {
        if (!frontier.isEmpty())
            return false;
        if (!Objects.equals(root, rootPos)) {
            rebuildContext(root);
            return false;
        }

        if (counterpartActed) {
            counterpartActed = false;
            softReset(root);
            return false;
        }

        if (affectedArea == null)
            affectedArea = BlockBox.create(root, root);

        World world = getWorld();
        if (!queue.isEmpty() && !isValid) {
            rebuildContext(root);
            return false;
        }

        if (validationFrontier.isEmpty() && !queue.isEmpty() && !simulate && revalidateIn == 0)
            revalidate(root);

        if (!simulate && infinite) {
            blockEntity.award(AllAdvancements.HOSE_PULLEY);
            if (FluidHelper.isLava(fluid))
                blockEntity.award(AllAdvancements.HOSE_PULLEY_LAVA);

            playEffect(world, root, fluid, true);
            return true;
        }

        while (!queue.isEmpty()) {
            // Dont dequeue here, so we can decide not to dequeue a valid entry when
            // simulating
            BlockPos currentPos = queue.first().pos();

            BlockState blockState = world.getBlockState(currentPos);
            BlockState emptied = blockState;
            Fluid fluid = Fluids.EMPTY;

            if (blockState.contains(Properties.WATERLOGGED) && blockState.get(Properties.WATERLOGGED)) {
                emptied = blockState.with(Properties.WATERLOGGED, Boolean.valueOf(false));
                fluid = Fluids.WATER;
            } else if (blockState.getBlock() instanceof FluidBlock flowingFluid) {
                emptied = Blocks.AIR.getDefaultState();
                if (blockState.get(FluidBlock.LEVEL) == 0)
                    fluid = flowingFluid.fluid;
                else {
                    affectedArea = BBHelper.encapsulate(affectedArea, BlockBox.create(currentPos, currentPos));
                    if (!blockEntity.isVirtual())
                        world.setBlockState(currentPos, emptied, 2 | 16);
                    queue.dequeue();
                    if (queue.isEmpty()) {
                        isValid = checkValid(world, rootPos);
                        reset();
                    }
                    continue;
                }
            } else if (blockState.getFluidState().getFluid() != Fluids.EMPTY && blockState.getCollisionShape(world, currentPos, ShapeContext.absent())
                .isEmpty()) {
                fluid = blockState.getFluidState().getFluid();
                emptied = Blocks.AIR.getDefaultState();
            }

            if (this.fluid == null)
                this.fluid = fluid;

            if (!this.fluid.matchesType(fluid)) {
                queue.dequeue();
                if (queue.isEmpty()) {
                    isValid = checkValid(world, rootPos);
                    reset();
                }
                continue;
            }

            if (simulate)
                return true;

            playEffect(world, currentPos, fluid, true);
            blockEntity.award(AllAdvancements.HOSE_PULLEY);

            if (!blockEntity.isVirtual()) {
                world.setBlockState(currentPos, emptied, 2 | 16);

                BlockState stateAbove = world.getBlockState(currentPos.up());
                if (stateAbove.getFluidState().getFluid() == Fluids.EMPTY && !stateAbove.canPlaceAt(world, currentPos.up()))
                    world.setBlockState(currentPos.up(), Blocks.AIR.getDefaultState(), 2 | 16);
            }
            affectedArea = BBHelper.encapsulate(affectedArea, currentPos);

            queue.dequeue();
            if (queue.isEmpty()) {
                isValid = checkValid(world, rootPos);
                reset();
            } else if (!validationSet.contains(currentPos)) {
                reset();
            }
            return true;
        }

        if (rootPos == null)
            return false;

        if (isValid)
            rebuildContext(root);

        return false;
    }

    protected void softReset(BlockPos root) {
        queue.clear();
        validationSet.clear();
        newValidationSet.clear();
        validationFrontier.clear();
        validationVisited.clear();
        visited.clear();
        infinite = false;
        setValidationTimer();
        frontier.add(new BlockPosEntry(root, 0));
        blockEntity.sendData();
    }

    protected boolean checkValid(World world, BlockPos root) {
        BlockPos currentPos = root;
        for (int timeout = 1000; timeout > 0 && !root.equals(blockEntity.getPos()); timeout--) {
            FluidBlockType canPullFluidsFrom = canPullFluidsFrom(world.getBlockState(currentPos), currentPos);
            if (canPullFluidsFrom == FluidBlockType.FLOWING) {
                for (Direction d : Iterate.directions) {
                    BlockPos side = currentPos.offset(d);
                    if (canPullFluidsFrom(world.getBlockState(side), side) == FluidBlockType.SOURCE)
                        return true;
                }
                currentPos = currentPos.up();
                continue;
            }
            if (canPullFluidsFrom == FluidBlockType.SOURCE)
                return true;
            break;
        }
        return false;
    }

    protected enum FluidBlockType {
        NONE,
        SOURCE,
        FLOWING;
    }

    @Override
    public void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        if (!clientPacket && affectedArea != null)
            frontier.add(new BlockPosEntry(rootPos, 0));
    }

    protected FluidBlockType canPullFluidsFrom(BlockState blockState, BlockPos pos) {
        if (blockState.contains(Properties.WATERLOGGED) && blockState.get(Properties.WATERLOGGED))
            return FluidBlockType.SOURCE;
        if (blockState.getBlock() instanceof FluidBlock)
            return blockState.get(FluidBlock.LEVEL) == 0 ? FluidBlockType.SOURCE : FluidBlockType.FLOWING;
        if (blockState.getFluidState().getFluid() != Fluids.EMPTY && blockState.getCollisionShape(getWorld(), pos, ShapeContext.absent()).isEmpty())
            return FluidBlockType.SOURCE;
        return FluidBlockType.NONE;
    }

    @Override
    public void tick() {
        super.tick();
        if (rootPos != null)
            isValid = checkValid(getWorld(), rootPos);
        if (!frontier.isEmpty()) {
            continueSearch();
            return;
        }
        if (!validationFrontier.isEmpty()) {
            continueValidation();
            return;
        }
        if (revalidateIn > 0)
            revalidateIn--;
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
    }

    public void rebuildContext(BlockPos root) {
        reset();
        rootPos = root;
        affectedArea = BlockBox.create(rootPos, rootPos);
        if (isValid)
            frontier.add(new BlockPosEntry(root, 0));
    }

    public void revalidate(BlockPos root) {
        validationFrontier.clear();
        validationVisited.clear();
        newValidationSet.clear();
        validationFrontier.add(new BlockPosEntry(root, 0));
        setValidationTimer();
    }

    private void continueSearch() {
        try {
            fluid = search(
                fluid, frontier, visited, (e, d) -> {
                    queue.enqueue(new BlockPosEntry(e, d));
                    validationSet.add(e);
                }, false
            );
        } catch (ChunkNotLoadedException e) {
            blockEntity.sendData();
            frontier.clear();
            visited.clear();
        }

        int maxBlocks = maxBlocks();
        if (visited.size() >= maxBlocks && canDrainInfinitely(fluid) && !queue.isEmpty()) {
            infinite = true;
            BlockPos firstValid = queue.first().pos();
            frontier.clear();
            visited.clear();
            queue.clear();
            queue.enqueue(new BlockPosEntry(firstValid, 0));
            blockEntity.sendData();
            return;
        }

        if (!frontier.isEmpty())
            return;

        blockEntity.sendData();
        visited.clear();
    }

    private void continueValidation() {
        try {
            search(fluid, validationFrontier, validationVisited, (e, d) -> newValidationSet.add(e), false);
        } catch (ChunkNotLoadedException e) {
            validationFrontier.clear();
            validationVisited.clear();
            setLongValidationTimer();
            return;
        }

        int maxBlocks = maxBlocks();
        if (validationVisited.size() >= maxBlocks && canDrainInfinitely(fluid)) {
            if (!infinite)
                reset();
            validationFrontier.clear();
            setLongValidationTimer();
            return;
        }

        if (!validationFrontier.isEmpty())
            return;
        if (infinite) {
            reset();
            return;
        }

        validationSet = newValidationSet;
        newValidationSet = new HashSet<>();
        validationVisited.clear();
    }

    @Override
    public void reset() {
        super.reset();

        fluid = null;
        rootPos = null;
        queue.clear();
        validationSet.clear();
        newValidationSet.clear();
        validationFrontier.clear();
        validationVisited.clear();
        blockEntity.sendData();
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    protected boolean isSearching() {
        return !frontier.isEmpty();
    }

    public FluidStack getDrainableFluid(BlockPos rootPos) {
        return fluid == null || isSearching() || !pullNext(rootPos, true) ? FluidStack.EMPTY : new FluidStack(fluid, BucketFluidInventory.CAPACITY);
    }

}

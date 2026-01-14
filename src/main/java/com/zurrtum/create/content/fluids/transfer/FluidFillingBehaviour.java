package com.zurrtum.create.content.fluids.transfer;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.BBHelper;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.tick.QueryableTickScheduler;
import net.minecraft.world.tick.WorldTickScheduler;

import java.util.*;

public class FluidFillingBehaviour extends FluidManipulationBehaviour {

    public static final BehaviourType<FluidFillingBehaviour> TYPE = new BehaviourType<>();

    PriorityQueue<BlockPosEntry> queue;

    List<BlockPosEntry> infinityCheckFrontier;
    Set<BlockPos> infinityCheckVisited;

    public FluidFillingBehaviour(SmartBlockEntity be) {
        super(be);
        queue = new ObjectHeapPriorityQueue<>((p, p2) -> -comparePositions(p, p2));
        revalidateIn = 1;
        infinityCheckFrontier = new ArrayList<>();
        infinityCheckVisited = new HashSet<>();
    }

    @Override
    public void tick() {
        super.tick();
        if (!infinityCheckFrontier.isEmpty() && rootPos != null) {
            Fluid fluid = getWorld().getFluidState(rootPos).getFluid();
            if (fluid != Fluids.EMPTY)
                continueValidation(fluid);
        }
        if (revalidateIn > 0)
            revalidateIn--;
    }

    protected void continueValidation(Fluid fluid) {
        try {
            search(fluid, infinityCheckFrontier, infinityCheckVisited, (p, d) -> infinityCheckFrontier.add(new BlockPosEntry(p, d)), true);
        } catch (ChunkNotLoadedException e) {
            infinityCheckFrontier.clear();
            infinityCheckVisited.clear();
            setLongValidationTimer();
            return;
        }

        int maxBlocks = maxBlocks();

        if (infinityCheckVisited.size() >= maxBlocks && maxBlocks != -1 && !fillInfinite()) {
            if (!infinite) {
                reset();
                infinite = true;
                blockEntity.sendData();
            }
            infinityCheckFrontier.clear();
            setLongValidationTimer();
            return;
        }

        if (!infinityCheckFrontier.isEmpty())
            return;
        if (infinite) {
            reset();
            return;
        }

        infinityCheckVisited.clear();
    }

    public boolean tryDeposit(Fluid fluid, BlockPos root, boolean simulate) {
        if (!Objects.equals(root, rootPos)) {
            reset();
            rootPos = root;
            queue.enqueue(new BlockPosEntry(root, 0));
            affectedArea = BlockBox.create(rootPos, rootPos);
            return false;
        }

        if (counterpartActed) {
            counterpartActed = false;
            softReset(root);
            return false;
        }

        if (affectedArea == null)
            affectedArea = BlockBox.create(root, root);

        if (revalidateIn == 0) {
            visited.clear();
            infinityCheckFrontier.clear();
            infinityCheckVisited.clear();
            infinityCheckFrontier.add(new BlockPosEntry(root, 0));
            setValidationTimer();
            softReset(root);
        }

        World world = getWorld();
        int maxRange = maxRange();
        int maxRangeSq = maxRange * maxRange;
        int maxBlocks = maxBlocks();
        boolean evaporate = world.getDimension().ultrawarm() && FluidHelper.isTag(fluid, FluidTags.WATER);
        boolean canPlaceSources = AllConfigs.server().fluids.fluidFillPlaceFluidSourceBlocks.get();

        if ((!fillInfinite() && infinite) || evaporate || !canPlaceSources) {
            FluidState fluidState = world.getFluidState(rootPos);
            boolean equivalentTo = fluidState.getFluid().matchesType(fluid);
            if (!equivalentTo && !evaporate && canPlaceSources)
                return false;
            if (simulate)
                return true;
            playEffect(world, root, fluid, false);
            if (evaporate) {
                int i = root.getX();
                int j = root.getY();
                int k = root.getZ();
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
            } else if (!canPlaceSources)
                blockEntity.award(AllAdvancements.HOSE_PULLEY);
            return true;
        }

        boolean success = false;
        for (int i = 0; !success && !queue.isEmpty() && i < searchedPerTick; i++) {
            BlockPosEntry entry = queue.first();
            BlockPos currentPos = entry.pos();

            if (visited.contains(currentPos)) {
                queue.dequeue();
                continue;
            }

            if (!simulate)
                visited.add(currentPos);

            if (visited.size() >= maxBlocks && maxBlocks != -1) {
                infinite = true;
                if (!fillInfinite()) {
                    visited.clear();
                    queue.clear();
                    return false;
                }
            }

            SpaceType spaceType = getAtPos(world, currentPos, fluid);
            if (spaceType == SpaceType.BLOCKING)
                continue;
            if (spaceType == SpaceType.FILLABLE) {
                success = true;
                if (!simulate) {
                    playEffect(world, currentPos, fluid, false);

                    BlockState blockState = world.getBlockState(currentPos);
                    if (blockState.contains(Properties.WATERLOGGED) && fluid.matchesType(Fluids.WATER)) {
                        if (!blockEntity.isVirtual())
                            world.setBlockState(currentPos, updatePostWaterlogging(blockState.with(Properties.WATERLOGGED, true)), 2 | 16);
                    } else {
                        replaceBlock(world, currentPos, blockState);
                        if (!blockEntity.isVirtual())
                            world.setBlockState(currentPos, FluidHelper.convertToStill(fluid).getDefaultState().getBlockState(), 2 | 16);
                    }

                    QueryableTickScheduler<Fluid> pendingFluidTicks = world.getFluidTickScheduler();
                    if (pendingFluidTicks instanceof WorldTickScheduler<Fluid> serverTickList) {
                        serverTickList.clearNextTicks(new BlockBox(currentPos));
                    }

                    affectedArea = BBHelper.encapsulate(affectedArea, currentPos);
                }
            }

            if (simulate && success)
                return true;

            visited.add(currentPos);
            queue.dequeue();

            for (Direction side : Iterate.directions) {
                if (side == Direction.UP)
                    continue;

                BlockPos offsetPos = currentPos.offset(side);
                if (visited.contains(offsetPos))
                    continue;
                if (offsetPos.getSquaredDistance(rootPos) > maxRangeSq)
                    continue;

                SpaceType nextSpaceType = getAtPos(world, offsetPos, fluid);
                if (nextSpaceType != SpaceType.BLOCKING)
                    queue.enqueue(new BlockPosEntry(offsetPos, entry.distance() + 1));
            }
        }

        if (!simulate && success)
            blockEntity.award(AllAdvancements.HOSE_PULLEY);
        return success;
    }

    protected void softReset(BlockPos root) {
        visited.clear();
        queue.clear();
        queue.enqueue(new BlockPosEntry(root, 0));
        infinite = false;
        setValidationTimer();
        blockEntity.sendData();
    }

    enum SpaceType {
        FILLABLE,
        FILLED,
        BLOCKING
    }

    protected SpaceType getAtPos(World world, BlockPos pos, Fluid toFill) {
        BlockState blockState = world.getBlockState(pos);
        FluidState fluidState = blockState.getFluidState();

        if (blockState.contains(Properties.WATERLOGGED))
            return toFill.matchesType(Fluids.WATER) ? blockState.get(Properties.WATERLOGGED) ? SpaceType.FILLED : SpaceType.FILLABLE : SpaceType.BLOCKING;

        if (blockState.getBlock() instanceof FluidBlock)
            return blockState.get(FluidBlock.LEVEL) == 0 ? toFill.matchesType(fluidState.getFluid()) ? SpaceType.FILLED : SpaceType.BLOCKING : SpaceType.FILLABLE;

        if (fluidState.getFluid() != Fluids.EMPTY && blockState.getCollisionShape(getWorld(), pos, ShapeContext.absent()).isEmpty())
            return toFill.matchesType(fluidState.getFluid()) ? SpaceType.FILLED : SpaceType.BLOCKING;

        return canBeReplacedByFluid(world, pos, blockState) ? SpaceType.FILLABLE : SpaceType.BLOCKING;
    }

    protected void replaceBlock(World world, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
        Block.dropStacks(state, world, pos, blockEntity);
    }

    // From FlowingFluidBlock#isBlocked
    protected boolean canBeReplacedByFluid(BlockView world, BlockPos pos, BlockState pState) {
        Block block = pState.getBlock();
        if (!(block instanceof DoorBlock) && !pState.isIn(BlockTags.ALL_SIGNS) && !pState.isOf(Blocks.LADDER) && !pState.isOf(Blocks.SUGAR_CANE) && !pState.isOf(
            Blocks.BUBBLE_COLUMN)) {
            if (!pState.isOf(Blocks.NETHER_PORTAL) && !pState.isOf(Blocks.END_PORTAL) && !pState.isOf(Blocks.END_GATEWAY) && !pState.isOf(Blocks.STRUCTURE_VOID)) {
                return !pState.blocksMovement();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    protected BlockState updatePostWaterlogging(BlockState state) {
        if (state.contains(Properties.LIT))
            state = state.with(Properties.LIT, false);
        return state;
    }

    @Override
    public void reset() {
        super.reset();
        queue.clear();
        infinityCheckFrontier.clear();
        infinityCheckVisited.clear();
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

}

package com.zurrtum.create.content.fluids.pipes;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.api.contraption.transformable.TransformableBlock;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.decoration.encasing.EncasedBlock;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.fluids.FluidPropagator;
import com.zurrtum.create.content.fluids.FluidTransportBehaviour;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.tick.TickPriority;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static net.minecraft.state.property.Properties.*;

public class EncasedPipeBlock extends Block implements IWrenchable, SpecialBlockItemRequirement, IBE<FluidPipeBlockEntity>, EncasedBlock, TransformableBlock, NeighborUpdateListeningBlock {
    public static final Map<Direction, BooleanProperty> FACING_TO_PROPERTY_MAP = ConnectingBlock.FACING_PROPERTIES;

    private final Block casing;

    public EncasedPipeBlock(Settings properties, Block casing) {
        super(properties);
        this.casing = casing;
        setDefaultState(getDefaultState().with(NORTH, false).with(SOUTH, false).with(DOWN, false).with(UP, false).with(WEST, false)
            .with(EAST, false));
    }

    public static EncasedPipeBlock copper(Settings properties) {
        return new EncasedPipeBlock(properties, AllBlocks.COPPER_CASING);
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
        super.appendProperties(builder);
    }

    @Override
    public void onPlaced(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.onPlaced(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    @Override
    public void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean isMoving) {
        if (!world.isClient)
            FluidPropagator.propagateChangedPipe(world, pos, state);
        if (state.hasBlockEntity())
            world.removeBlockEntity(pos);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!world.isClient && state != oldState)
            world.scheduleBlockTick(pos, this, 1, TickPriority.HIGH);
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return AllItems.FLUID_PIPE.getDefaultStack();
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block otherBlock, BlockPos neighborPos, boolean isMoving) {
        Direction d = FluidPropagator.validateNeighbourChange(state, world, pos, otherBlock, neighborPos, isMoving);
        if (d == null)
            return;
        if (!state.get(FACING_TO_PROPERTY_MAP.get(d)))
            return;
        world.scheduleBlockTick(pos, this, 1, TickPriority.HIGH);
    }

    @Override
    public void neighborUpdate(
        BlockState state,
        World world,
        BlockPos pos,
        Block otherBlock,
        @Nullable WireOrientation wireOrientation,
        boolean isMoving
    ) {
        DebugInfoSender.sendNeighborUpdate(world, pos);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random r) {
        FluidPropagator.propagateChangedPipe(world, pos, state);
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();

        if (world.isClient)
            return ActionResult.SUCCESS;

        context.getWorld().syncWorldEvent(WorldEvents.BLOCK_BROKEN, context.getBlockPos(), Block.getRawIdFromState(state));
        BlockState equivalentPipe = transferSixWayProperties(state, AllBlocks.FLUID_PIPE.getDefaultState());

        Direction firstFound = Direction.UP;
        for (Direction d : Iterate.directions)
            if (state.get(FACING_TO_PROPERTY_MAP.get(d))) {
                firstFound = d;
                break;
            }

        FluidTransportBehaviour.cacheFlows(world, pos);
        world.setBlockState(pos, AllBlocks.FLUID_PIPE.updateBlockState(equivalentPipe, firstFound, null, world, pos));
        FluidTransportBehaviour.loadFlows(world, pos);
        return ActionResult.SUCCESS;
    }

    public static BlockState transferSixWayProperties(BlockState from, BlockState to) {
        for (Direction d : Iterate.directions) {
            BooleanProperty property = FACING_TO_PROPERTY_MAP.get(d);
            to = to.with(property, from.get(property));
        }
        return to;
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity be) {
        return ItemRequirement.of(AllBlocks.FLUID_PIPE.getDefaultState(), be);
    }

    @Override
    public Class<FluidPipeBlockEntity> getBlockEntityClass() {
        return FluidPipeBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FluidPipeBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.ENCASED_FLUID_PIPE;
    }

    @Override
    public Block getCasing() {
        return casing;
    }

    @Override
    public void handleEncasing(BlockState state, World level, BlockPos pos, ItemStack heldItem, PlayerEntity player, Hand hand, BlockHitResult ray) {
        FluidTransportBehaviour.cacheFlows(level, pos);
        level.setBlockState(pos, EncasedPipeBlock.transferSixWayProperties(state, getDefaultState()));
        FluidTransportBehaviour.loadFlows(level, pos);
    }

    @Override
    public BlockState rotate(BlockState pState, BlockRotation pRotation) {
        return FluidPipeBlockRotation.rotate(pState, pRotation);
    }

    @Override
    public BlockState mirror(BlockState pState, BlockMirror pMirror) {
        return FluidPipeBlockRotation.mirror(pState, pMirror);
    }

    @Override
    public BlockState transform(BlockState state, StructureTransform transform) {
        return FluidPipeBlockRotation.transform(state, transform);
    }

}

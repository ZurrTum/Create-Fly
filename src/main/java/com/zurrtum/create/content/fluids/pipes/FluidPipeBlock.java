package com.zurrtum.create.content.fluids.pipes;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.contraption.transformable.TransformableBlock;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.zurrtum.create.content.decoration.encasing.EncasableBlock;
import com.zurrtum.create.content.equipment.wrench.IWrenchableWithBracket;
import com.zurrtum.create.content.fluids.FluidPropagator;
import com.zurrtum.create.content.fluids.FluidTransportBehaviour;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.tick.ScheduledTickView;
import net.minecraft.world.tick.TickPriority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;

public class FluidPipeBlock extends ConnectingBlock implements Waterloggable, IWrenchableWithBracket, IBE<FluidPipeBlockEntity>, EncasableBlock, TransformableBlock, NeighborUpdateListeningBlock {

    private static final VoxelShape OCCLUSION_BOX = Block.createCuboidShape(4, 4, 4, 12, 12, 12);

    public static final MapCodec<FluidPipeBlock> CODEC = createCodec(FluidPipeBlock::new);

    public FluidPipeBlock(Settings properties) {
        super(8f, properties);
        setDefaultState(getDefaultState().with(Properties.WATERLOGGED, false));
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        if (tryRemoveBracket(context))
            return ActionResult.SUCCESS;

        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        Direction clickedFace = context.getSide();

        Axis axis = getAxis(world, pos, state);
        if (axis == null) {
            Vec3d clickLocation = context.getHitPos().subtract(pos.getX(), pos.getY(), pos.getZ());
            double closest = Float.MAX_VALUE;
            Direction argClosest = Direction.UP;
            for (Direction direction : Iterate.directions) {
                if (clickedFace.getAxis() == direction.getAxis())
                    continue;
                Vec3d centerOf = Vec3d.ofCenter(direction.getVector());
                double distance = centerOf.squaredDistanceTo(clickLocation);
                if (distance < closest) {
                    closest = distance;
                    argClosest = direction;
                }
            }
            axis = argClosest.getAxis();
        }

        if (clickedFace.getAxis() == axis)
            return ActionResult.PASS;
        if (!world.isClient()) {
            withBlockEntityDo(
                world,
                pos,
                fpte -> fpte.getBehaviour(FluidTransportBehaviour.TYPE).interfaces.values().stream().filter(pc -> pc != null && pc.hasFlow())
                    .findAny().ifPresent($ -> AllAdvancements.GLASS_PIPE.trigger((ServerPlayerEntity) context.getPlayer()))
            );

            FluidTransportBehaviour.cacheFlows(world, pos);
            world.setBlockState(
                pos,
                AllBlocks.GLASS_FLUID_PIPE.getDefaultState().with(GlassFluidPipeBlock.AXIS, axis)
                    .with(Properties.WATERLOGGED, state.get(Properties.WATERLOGGED))
            );
            FluidTransportBehaviour.loadFlows(world, pos);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onPlaced(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.onPlaced(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    @Override
    protected ActionResult onUseWithItem(
        ItemStack stack,
        BlockState state,
        World level,
        BlockPos pos,
        PlayerEntity player,
        Hand hand,
        BlockHitResult hitResult
    ) {
        ActionResult result = tryEncase(state, level, pos, stack, player, hand, hitResult);
        if (result.isAccepted())
            return result;

        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    public BlockState getAxisState(Axis axis) {
        BlockState defaultState = getDefaultState();
        for (Direction d : Iterate.directions)
            defaultState = defaultState.with(FACING_PROPERTIES.get(d), d.getAxis() == axis);
        return defaultState;
    }

    @Nullable
    private Axis getAxis(BlockView world, BlockPos pos, BlockState state) {
        return FluidPropagator.getStraightPipeAxis(state);
    }

    @Override
    public void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean isMoving) {
        if (!world.isClient())
            FluidPropagator.propagateChangedPipe(world, pos, state);
        if (!isMoving)
            removeBracket(world, pos, true).ifPresent(stack -> Block.dropStack(world, pos, stack));
        if (state.hasBlockEntity())
            world.removeBlockEntity(pos);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (world.isClient())
            return;
        if (state != oldState)
            world.scheduleBlockTick(pos, this, 1, TickPriority.HIGH);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block otherBlock, BlockPos neighborPos, boolean isMoving) {
        Direction d = FluidPropagator.validateNeighbourChange(state, world, pos, otherBlock, neighborPos, isMoving);
        if (d == null)
            return;
        if (!isOpenAt(state, d))
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

    public static boolean isPipe(BlockState state) {
        return state.getBlock() instanceof FluidPipeBlock;
    }

    public static boolean canConnectTo(BlockRenderView world, BlockPos neighbourPos, BlockState neighbour, Direction direction) {
        if (FluidPropagator.hasFluidCapability(world, neighbourPos, direction.getOpposite()))
            return true;
        if (VanillaFluidTargets.canProvideFluidWithoutCapability(neighbour))
            return true;
        if (isPipe(neighbour)) {
            BracketedBlockEntityBehaviour bracket = BlockEntityBehaviour.get(world, neighbourPos, BracketedBlockEntityBehaviour.TYPE);
            return bracket == null || !bracket.isBracketPresent() || FluidPropagator.getStraightPipeAxis(neighbour) == direction.getAxis();
        }
        FluidTransportBehaviour transport = BlockEntityBehaviour.get(world, neighbourPos, FluidTransportBehaviour.TYPE);
        if (transport == null)
            return false;
        return transport.canHaveFlowToward(neighbour, direction.getOpposite());
    }

    public static boolean shouldDrawRim(BlockRenderView world, BlockPos pos, BlockState state, Direction direction) {
        BlockPos offsetPos = pos.offset(direction);
        BlockState facingState = world.getBlockState(offsetPos);
        if (facingState.getBlock() instanceof EncasedPipeBlock)
            return true;
        if (!isPipe(facingState))
            return true;
        return !canConnectTo(world, offsetPos, facingState, direction);
    }

    public static boolean isOpenAt(BlockState state, Direction direction) {
        return state.get(FACING_PROPERTIES.get(direction));
    }

    public static boolean isCornerOrEndPipe(BlockRenderView world, BlockPos pos, BlockState state) {
        return isPipe(state) && FluidPropagator.getStraightPipeAxis(state) == null && !shouldDrawCasing(world, pos, state);
    }

    public static boolean shouldDrawCasing(BlockRenderView world, BlockPos pos, BlockState state) {
        if (!isPipe(state))
            return false;
        for (Axis axis : Iterate.axes) {
            int connections = 0;
            for (Direction direction : Iterate.directions)
                if (direction.getAxis() != axis && isOpenAt(state, direction))
                    connections++;
            if (connections > 2)
                return true;
        }
        return false;
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, Properties.WATERLOGGED);
        super.appendProperties(builder);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        FluidState FluidState = context.getWorld().getFluidState(context.getBlockPos());
        return updateBlockState(getDefaultState(), context.getPlayerLookDirection(), null, context.getWorld(), context.getBlockPos()).with(
            Properties.WATERLOGGED,
            Boolean.valueOf(FluidState.getFluid() == Fluids.WATER)
        );
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState state,
        WorldView world,
        ScheduledTickView tickView,
        BlockPos pos,
        Direction direction,
        BlockPos neighbourPos,
        BlockState neighbourState,
        Random random
    ) {
        if (state.get(Properties.WATERLOGGED))
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        if (isOpenAt(state, direction) && neighbourState.contains(Properties.WATERLOGGED))
            tickView.scheduleBlockTick(pos, this, 1, TickPriority.HIGH);
        return updateBlockState(state, direction, direction.getOpposite(), world, pos);
    }

    public BlockState updateBlockState(
        BlockState state,
        Direction preferredDirection,
        @Nullable Direction ignore,
        BlockRenderView world,
        BlockPos pos
    ) {

        BracketedBlockEntityBehaviour bracket = BlockEntityBehaviour.get(world, pos, BracketedBlockEntityBehaviour.TYPE);
        if (bracket != null && bracket.isBracketPresent())
            return state;

        BlockState prevState = state;
        int prevStateSides = (int) Arrays.stream(Iterate.directions).map(FACING_PROPERTIES::get).filter(prevState::get).count();

        // Update sides that are not ignored
        for (Direction d : Iterate.directions)
            if (d != ignore) {
                boolean shouldConnect = canConnectTo(world, pos.offset(d), world.getBlockState(pos.offset(d)), d);
                state = state.with(FACING_PROPERTIES.get(d), shouldConnect);
            }

        // See if it has enough connections
        Direction connectedDirection = null;
        for (Direction d : Iterate.directions) {
            if (isOpenAt(state, d)) {
                if (connectedDirection != null)
                    return state;
                connectedDirection = d;
            }
        }

        // Add opposite end if only one connection
        if (connectedDirection != null)
            return state.with(FACING_PROPERTIES.get(connectedDirection.getOpposite()), true);

        // If we can't connect to anything and weren't connected before, do nothing
        if (prevStateSides == 2)
            return prevState;

        // Use preferred
        return state.with(FACING_PROPERTIES.get(preferredDirection), true).with(FACING_PROPERTIES.get(preferredDirection.getOpposite()), true);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(Properties.WATERLOGGED) ? Fluids.WATER.getStill(false) : Fluids.EMPTY.getDefaultState();
    }

    @Override
    public Optional<ItemStack> removeBracket(BlockView world, BlockPos pos, boolean inOnReplacedContext) {
        BracketedBlockEntityBehaviour behaviour = BracketedBlockEntityBehaviour.get(world, pos, BracketedBlockEntityBehaviour.TYPE);
        if (behaviour == null)
            return Optional.empty();
        BlockState bracket = behaviour.removeBracket(inOnReplacedContext);
        if (bracket == null)
            return Optional.empty();
        return Optional.of(new ItemStack(bracket.getBlock()));
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    @Override
    public Class<FluidPipeBlockEntity> getBlockEntityClass() {
        return FluidPipeBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FluidPipeBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.FLUID_PIPE;
    }

    @Override
    public VoxelShape getCullingShape(BlockState pState) {
        return OCCLUSION_BOX;
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

    @Override
    protected @NotNull MapCodec<? extends ConnectingBlock> getCodec() {
        return CODEC;
    }
}

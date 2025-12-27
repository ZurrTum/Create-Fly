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
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;

public class FluidPipeBlock extends PipeBlock implements SimpleWaterloggedBlock, IWrenchableWithBracket, IBE<FluidPipeBlockEntity>, EncasableBlock, TransformableBlock, NeighborUpdateListeningBlock {

    private static final VoxelShape OCCLUSION_BOX = Block.box(4, 4, 4, 12, 12, 12);

    public static final MapCodec<FluidPipeBlock> CODEC = simpleCodec(FluidPipeBlock::new);

    public FluidPipeBlock(Properties properties) {
        super(8f, properties);
        registerDefaultState(defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if (tryRemoveBracket(context))
            return InteractionResult.SUCCESS;

        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();

        Axis axis = getAxis(world, pos, state);
        if (axis == null) {
            Vec3 clickLocation = context.getClickLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
            double closest = Float.MAX_VALUE;
            Direction argClosest = Direction.UP;
            for (Direction direction : Iterate.directions) {
                if (clickedFace.getAxis() == direction.getAxis())
                    continue;
                Vec3 centerOf = Vec3.atCenterOf(direction.getUnitVec3i());
                double distance = centerOf.distanceToSqr(clickLocation);
                if (distance < closest) {
                    closest = distance;
                    argClosest = direction;
                }
            }
            axis = argClosest.getAxis();
        }

        if (clickedFace.getAxis() == axis)
            return InteractionResult.PASS;
        if (!world.isClientSide()) {
            withBlockEntityDo(
                world,
                pos,
                fpte -> fpte.getBehaviour(FluidTransportBehaviour.TYPE).interfaces.values().stream().filter(pc -> pc != null && pc.hasFlow())
                    .findAny().ifPresent($ -> AllAdvancements.GLASS_PIPE.trigger((ServerPlayer) context.getPlayer()))
            );

            FluidTransportBehaviour.cacheFlows(world, pos);
            world.setBlockAndUpdate(
                pos,
                AllBlocks.GLASS_FLUID_PIPE.defaultBlockState().setValue(GlassFluidPipeBlock.AXIS, axis)
                    .setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED))
            );
            FluidTransportBehaviour.loadFlows(world, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    @Override
    protected InteractionResult useItemOn(
        ItemStack stack,
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        InteractionHand hand,
        BlockHitResult hitResult
    ) {
        InteractionResult result = tryEncase(state, level, pos, stack, player, hand, hitResult);
        if (result.consumesAction())
            return result;

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    public BlockState getAxisState(Axis axis) {
        BlockState defaultState = defaultBlockState();
        for (Direction d : Iterate.directions)
            defaultState = defaultState.setValue(PROPERTY_BY_DIRECTION.get(d), d.getAxis() == axis);
        return defaultState;
    }

    @Nullable
    private Axis getAxis(BlockGetter world, BlockPos pos, BlockState state) {
        return FluidPropagator.getStraightPipeAxis(state);
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean isMoving) {
        if (!world.isClientSide())
            FluidPropagator.propagateChangedPipe(world, pos, state);
        if (!isMoving)
            removeBracket(world, pos, true).ifPresent(stack -> Block.popResource(world, pos, stack));
        if (state.hasBlockEntity())
            world.removeBlockEntity(pos);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (world.isClientSide())
            return;
        if (state != oldState)
            world.scheduleTick(pos, this, 1, TickPriority.HIGH);
    }

    @Override
    public void neighborUpdate(BlockState state, Level world, BlockPos pos, Block otherBlock, BlockPos neighborPos, boolean isMoving) {
        Direction d = FluidPropagator.validateNeighbourChange(state, world, pos, otherBlock, neighborPos, isMoving);
        if (d == null)
            return;
        if (!isOpenAt(state, d))
            return;
        world.scheduleTick(pos, this, 1, TickPriority.HIGH);
    }

    @Override
    public void neighborChanged(
        BlockState state,
        Level world,
        BlockPos pos,
        Block otherBlock,
        @Nullable Orientation wireOrientation,
        boolean isMoving
    ) {
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource r) {
        FluidPropagator.propagateChangedPipe(world, pos, state);
    }

    public static boolean isPipe(BlockState state) {
        return state.getBlock() instanceof FluidPipeBlock;
    }

    public static boolean canConnectTo(BlockAndTintGetter world, BlockPos neighbourPos, BlockState neighbour, Direction direction) {
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

    public static boolean shouldDrawRim(BlockAndTintGetter world, BlockPos pos, BlockState state, Direction direction) {
        BlockPos offsetPos = pos.relative(direction);
        BlockState facingState = world.getBlockState(offsetPos);
        if (facingState.getBlock() instanceof EncasedPipeBlock)
            return true;
        if (!isPipe(facingState))
            return true;
        return !canConnectTo(world, offsetPos, facingState, direction);
    }

    public static boolean isOpenAt(BlockState state, Direction direction) {
        return state.getValue(PROPERTY_BY_DIRECTION.get(direction));
    }

    public static boolean isCornerOrEndPipe(BlockAndTintGetter world, BlockPos pos, BlockState state) {
        return isPipe(state) && FluidPropagator.getStraightPipeAxis(state) == null && !shouldDrawCasing(world, pos, state);
    }

    public static boolean shouldDrawCasing(BlockAndTintGetter world, BlockPos pos, BlockState state) {
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
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, BlockStateProperties.WATERLOGGED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState FluidState = context.getLevel().getFluidState(context.getClickedPos());
        return updateBlockState(
            defaultBlockState(),
            context.getNearestLookingDirection(),
            null,
            context.getLevel(),
            context.getClickedPos()
        ).setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(FluidState.getType() == Fluids.WATER));
    }

    @Override
    public BlockState updateShape(
        BlockState state,
        LevelReader world,
        ScheduledTickAccess tickView,
        BlockPos pos,
        Direction direction,
        BlockPos neighbourPos,
        BlockState neighbourState,
        RandomSource random
    ) {
        if (state.getValue(BlockStateProperties.WATERLOGGED))
            tickView.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        if (isOpenAt(state, direction) && neighbourState.hasProperty(BlockStateProperties.WATERLOGGED))
            tickView.scheduleTick(pos, this, 1, TickPriority.HIGH);
        return updateBlockState(state, direction, direction.getOpposite(), world, pos);
    }

    public BlockState updateBlockState(
        BlockState state,
        Direction preferredDirection,
        @Nullable Direction ignore,
        BlockAndTintGetter world,
        BlockPos pos
    ) {

        BracketedBlockEntityBehaviour bracket = BlockEntityBehaviour.get(world, pos, BracketedBlockEntityBehaviour.TYPE);
        if (bracket != null && bracket.isBracketPresent())
            return state;

        BlockState prevState = state;
        int prevStateSides = (int) Arrays.stream(Iterate.directions).map(PROPERTY_BY_DIRECTION::get).filter(prevState::getValue).count();

        // Update sides that are not ignored
        for (Direction d : Iterate.directions)
            if (d != ignore) {
                boolean shouldConnect = canConnectTo(world, pos.relative(d), world.getBlockState(pos.relative(d)), d);
                state = state.setValue(PROPERTY_BY_DIRECTION.get(d), shouldConnect);
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
            return state.setValue(PROPERTY_BY_DIRECTION.get(connectedDirection.getOpposite()), true);

        // If we can't connect to anything and weren't connected before, do nothing
        if (prevStateSides == 2)
            return prevState;

        // Use preferred
        return state.setValue(PROPERTY_BY_DIRECTION.get(preferredDirection), true)
            .setValue(PROPERTY_BY_DIRECTION.get(preferredDirection.getOpposite()), true);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public Optional<ItemStack> removeBracket(BlockGetter world, BlockPos pos, boolean inOnReplacedContext) {
        BracketedBlockEntityBehaviour behaviour = BracketedBlockEntityBehaviour.get(world, pos, BracketedBlockEntityBehaviour.TYPE);
        if (behaviour == null)
            return Optional.empty();
        BlockState bracket = behaviour.removeBracket(inOnReplacedContext);
        if (bracket == null)
            return Optional.empty();
        return Optional.of(new ItemStack(bracket.getBlock()));
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
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
    public VoxelShape getOcclusionShape(BlockState pState) {
        return OCCLUSION_BOX;
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return FluidPipeBlockRotation.rotate(pState, pRotation);
    }

    @Override
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return FluidPipeBlockRotation.mirror(pState, pMirror);
    }

    @Override
    public BlockState transform(BlockState state, StructureTransform transform) {
        return FluidPipeBlockRotation.transform(state, transform);
    }

    @Override
    protected @NotNull MapCodec<? extends PipeBlock> codec() {
        return CODEC;
    }
}

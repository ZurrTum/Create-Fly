package com.zurrtum.create.content.logistics.tunnel;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.levelWrappers.WrappedLevel;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltSlope;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.logistics.funnel.BeltFunnelBlock;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BeltTunnelBlock extends Block implements IBE<BeltTunnelBlockEntity>, ItemInventoryProvider<BeltTunnelBlockEntity>, NeighborUpdateListeningBlock, IWrenchable {

    public static final EnumProperty<Shape> SHAPE = EnumProperty.create("shape", Shape.class);
    public static final EnumProperty<Axis> HORIZONTAL_AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    public BeltTunnelBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(SHAPE, Shape.STRAIGHT));
    }

    @Override
    public Container getInventory(LevelAccessor world, BlockPos pos, BlockState state, BeltTunnelBlockEntity blockEntity, Direction context) {
        if (blockEntity.cap == null) {
            BlockState downState = world.getBlockState(pos.below());
            if (downState.is(AllBlocks.BELT)) {
                BlockEntity beBelow = world.getBlockEntity(pos.below());
                if (beBelow != null) {
                    Container capBelow = AllBlocks.BELT.getInventory(world, pos.below(), downState, (BeltBlockEntity) beBelow, Direction.UP);
                    if (capBelow != null) {
                        blockEntity.cap = capBelow;
                    }
                }
            }
        }
        return blockEntity.cap;
    }

    public enum Shape implements StringRepresentable {
        STRAIGHT,
        WINDOW,
        CLOSED,
        T_LEFT,
        T_RIGHT,
        CROSS;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return BeltTunnelShapes.getShape(state);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        BlockState blockState = worldIn.getBlockState(pos.below());
        if (!isValidPositionForPlacement(state, worldIn, pos))
            return false;
        return blockState.getValue(BeltBlock.CASING);
    }

    public boolean isValidPositionForPlacement(BlockState state, LevelReader worldIn, BlockPos pos) {
        BlockState blockState = worldIn.getBlockState(pos.below());
        if (!blockState.is(AllBlocks.BELT))
            return false;
        return blockState.getValue(BeltBlock.SLOPE) == BeltSlope.HORIZONTAL;
    }

    public static boolean hasWindow(BlockState state) {
        return state.getValue(SHAPE) == Shape.WINDOW || state.getValue(SHAPE) == Shape.CLOSED;
    }

    public static boolean isStraight(BlockState state) {
        return hasWindow(state) || state.getValue(SHAPE) == Shape.STRAIGHT;
    }

    public static boolean isJunction(BlockState state) {
        Shape shape = state.getValue(SHAPE);
        return shape == Shape.CROSS || shape == Shape.T_LEFT || shape == Shape.T_RIGHT;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return getTunnelState(context.getLevel(), context.getClickedPos());
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState p_220082_4_, boolean p_220082_5_) {
        if (!(world instanceof WrappedLevel) && !world.isClientSide())
            withBlockEntityDo(world, pos, BeltTunnelBlockEntity::updateTunnelConnections);
    }

    @Override
    public BlockState updateShape(
        BlockState state,
        LevelReader worldIn,
        ScheduledTickAccess tickView,
        BlockPos currentPos,
        Direction facing,
        BlockPos facingPos,
        BlockState facingState,
        RandomSource random
    ) {
        if (facing.getAxis().isVertical())
            return state;
        if (!(worldIn instanceof WrappedLevel) && !worldIn.isClientSide())
            withBlockEntityDo(worldIn, currentPos, BeltTunnelBlockEntity::updateTunnelConnections);
        BlockState tunnelState = getTunnelState(worldIn, currentPos);
        if (tunnelState.getValue(HORIZONTAL_AXIS) == state.getValue(HORIZONTAL_AXIS)) {
            if (hasWindow(tunnelState) == hasWindow(state))
                return state;
        }

        return tunnelState;
    }

    public void updateTunnel(LevelAccessor world, BlockPos pos) {
        BlockState tunnel = world.getBlockState(pos);
        BlockState newTunnel = getTunnelState(world, pos);
        if (tunnel != newTunnel && !world.isClientSide()) {
            world.setBlock(pos, newTunnel, 3);
            BlockEntity be = world.getBlockEntity(pos);
            if ((be instanceof BeltTunnelBlockEntity))
                ((BeltTunnelBlockEntity) be).updateTunnelConnections();
        }
    }

    private BlockState getTunnelState(BlockGetter reader, BlockPos pos) {
        BlockState state = defaultBlockState();
        BlockState belt = reader.getBlockState(pos.below());
        if (belt.is(AllBlocks.BELT))
            state = state.setValue(HORIZONTAL_AXIS, belt.getValue(BeltBlock.HORIZONTAL_FACING).getAxis());
        Axis axis = state.getValue(HORIZONTAL_AXIS);

        // T and Cross
        Direction left = Direction.get(AxisDirection.POSITIVE, axis).getClockWise();
        boolean onLeft = hasValidOutput(reader, pos.below(), left);
        boolean onRight = hasValidOutput(reader, pos.below(), left.getOpposite());

        if (onLeft && onRight)
            state = state.setValue(SHAPE, Shape.CROSS);
        else if (onLeft)
            state = state.setValue(SHAPE, Shape.T_LEFT);
        else if (onRight)
            state = state.setValue(SHAPE, Shape.T_RIGHT);

        if (state.getValue(SHAPE) == Shape.STRAIGHT) {
            boolean canHaveWindow = canHaveWindow(reader, pos, axis);
            if (canHaveWindow)
                state = state.setValue(SHAPE, Shape.WINDOW);
        }

        return state;
    }

    protected boolean canHaveWindow(BlockGetter reader, BlockPos pos, Axis axis) {
        Direction fw = Direction.get(AxisDirection.POSITIVE, axis);
        BlockState blockState1 = reader.getBlockState(pos.relative(fw));
        BlockState blockState2 = reader.getBlockState(pos.relative(fw.getOpposite()));

        boolean funnel1 = blockState1.getBlock() instanceof BeltFunnelBlock && blockState1.getValue(BeltFunnelBlock.SHAPE) == BeltFunnelBlock.Shape.EXTENDED && blockState1.getValue(
            BeltFunnelBlock.HORIZONTAL_FACING) == fw.getOpposite();
        boolean funnel2 = blockState2.getBlock() instanceof BeltFunnelBlock && blockState2.getValue(BeltFunnelBlock.SHAPE) == BeltFunnelBlock.Shape.EXTENDED && blockState2.getValue(
            BeltFunnelBlock.HORIZONTAL_FACING) == fw;

        boolean valid1 = blockState1.getBlock() instanceof BeltTunnelBlock || funnel1;
        boolean valid2 = blockState2.getBlock() instanceof BeltTunnelBlock || funnel2;
        boolean canHaveWindow = valid1 && valid2 && !(funnel1 && funnel2);
        return canHaveWindow;
    }

    private boolean hasValidOutput(BlockGetter world, BlockPos pos, Direction side) {
        BlockState blockState = world.getBlockState(pos.relative(side));
        if (blockState.is(AllBlocks.BELT))
            return blockState.getValue(BeltBlock.HORIZONTAL_FACING).getAxis() == side.getAxis();
        DirectBeltInputBehaviour behaviour = BlockEntityBehaviour.get(world, pos.relative(side), DirectBeltInputBehaviour.TYPE);
        return behaviour != null && behaviour.canInsertFromSide(side);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if (!hasWindow(state))
            return InteractionResult.PASS;

        // Toggle windows
        Shape shape = state.getValue(SHAPE);
        shape = shape == Shape.CLOSED ? Shape.WINDOW : Shape.CLOSED;
        Level world = context.getLevel();
        if (!world.isClientSide())
            world.setBlock(context.getClickedPos(), state.setValue(SHAPE, shape), Block.UPDATE_CLIENTS);
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        Direction fromAxis = Direction.get(AxisDirection.POSITIVE, state.getValue(HORIZONTAL_AXIS));
        Direction rotated = rotation.rotate(fromAxis);

        return state.setValue(HORIZONTAL_AXIS, rotated.getAxis());
    }

    @Override
    public void neighborUpdate(BlockState state, Level worldIn, BlockPos pos, Block sourceBlock, BlockPos fromPos, boolean isMoving) {
        if (worldIn.isClientSide())
            return;

        if (fromPos.equals(pos.below())) {
            if (!canSurvive(state, worldIn, pos)) {
                worldIn.destroyBlock(pos, true);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_AXIS, SHAPE);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public Class<BeltTunnelBlockEntity> getBlockEntityClass() {
        return BeltTunnelBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BeltTunnelBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.ANDESITE_TUNNEL;
    }

}

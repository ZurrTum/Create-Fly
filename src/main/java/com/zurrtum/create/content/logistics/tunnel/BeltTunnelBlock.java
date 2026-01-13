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
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

import java.util.Locale;

public class BeltTunnelBlock extends Block implements IBE<BeltTunnelBlockEntity>, ItemInventoryProvider<BeltTunnelBlockEntity>, NeighborUpdateListeningBlock, IWrenchable {

    public static final EnumProperty<Shape> SHAPE = EnumProperty.of("shape", Shape.class);
    public static final EnumProperty<Axis> HORIZONTAL_AXIS = Properties.HORIZONTAL_AXIS;

    public BeltTunnelBlock(Settings properties) {
        super(properties);
        setDefaultState(getDefaultState().with(SHAPE, Shape.STRAIGHT));
    }

    @Override
    public Inventory getInventory(WorldAccess world, BlockPos pos, BlockState state, BeltTunnelBlockEntity blockEntity, Direction context) {
        if (blockEntity.cap == null) {
            BlockState downState = world.getBlockState(pos.down());
            if (downState.isOf(AllBlocks.BELT)) {
                BlockEntity beBelow = world.getBlockEntity(pos.down());
                if (beBelow != null) {
                    Inventory capBelow = AllBlocks.BELT.getInventory(world, pos.down(), downState, (BeltBlockEntity) beBelow, Direction.UP);
                    if (capBelow != null) {
                        blockEntity.cap = capBelow;
                    }
                }
            }
        }
        return blockEntity.cap;
    }

    public enum Shape implements StringIdentifiable {
        STRAIGHT,
        WINDOW,
        CLOSED,
        T_LEFT,
        T_RIGHT,
        CROSS;

        @Override
        public String asString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return BeltTunnelShapes.getShape(state);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView worldIn, BlockPos pos) {
        BlockState blockState = worldIn.getBlockState(pos.down());
        if (!isValidPositionForPlacement(state, worldIn, pos))
            return false;
        return blockState.get(BeltBlock.CASING);
    }

    public boolean isValidPositionForPlacement(BlockState state, WorldView worldIn, BlockPos pos) {
        BlockState blockState = worldIn.getBlockState(pos.down());
        if (!blockState.isOf(AllBlocks.BELT))
            return false;
        return blockState.get(BeltBlock.SLOPE) == BeltSlope.HORIZONTAL;
    }

    public static boolean hasWindow(BlockState state) {
        return state.get(SHAPE) == Shape.WINDOW || state.get(SHAPE) == Shape.CLOSED;
    }

    public static boolean isStraight(BlockState state) {
        return hasWindow(state) || state.get(SHAPE) == Shape.STRAIGHT;
    }

    public static boolean isJunction(BlockState state) {
        Shape shape = state.get(SHAPE);
        return shape == Shape.CROSS || shape == Shape.T_LEFT || shape == Shape.T_RIGHT;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return getTunnelState(context.getWorld(), context.getBlockPos());
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState p_220082_4_, boolean p_220082_5_) {
        if (!(world instanceof WrappedLevel) && !world.isClient())
            withBlockEntityDo(world, pos, BeltTunnelBlockEntity::updateTunnelConnections);
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState state,
        WorldView worldIn,
        ScheduledTickView tickView,
        BlockPos currentPos,
        Direction facing,
        BlockPos facingPos,
        BlockState facingState,
        Random random
    ) {
        if (facing.getAxis().isVertical())
            return state;
        if (!(worldIn instanceof WrappedLevel) && !worldIn.isClient())
            withBlockEntityDo(worldIn, currentPos, BeltTunnelBlockEntity::updateTunnelConnections);
        BlockState tunnelState = getTunnelState(worldIn, currentPos);
        if (tunnelState.get(HORIZONTAL_AXIS) == state.get(HORIZONTAL_AXIS)) {
            if (hasWindow(tunnelState) == hasWindow(state))
                return state;
        }

        return tunnelState;
    }

    public void updateTunnel(WorldAccess world, BlockPos pos) {
        BlockState tunnel = world.getBlockState(pos);
        BlockState newTunnel = getTunnelState(world, pos);
        if (tunnel != newTunnel && !world.isClient()) {
            world.setBlockState(pos, newTunnel, 3);
            BlockEntity be = world.getBlockEntity(pos);
            if ((be instanceof BeltTunnelBlockEntity))
                ((BeltTunnelBlockEntity) be).updateTunnelConnections();
        }
    }

    private BlockState getTunnelState(BlockView reader, BlockPos pos) {
        BlockState state = getDefaultState();
        BlockState belt = reader.getBlockState(pos.down());
        if (belt.isOf(AllBlocks.BELT))
            state = state.with(HORIZONTAL_AXIS, belt.get(BeltBlock.HORIZONTAL_FACING).getAxis());
        Axis axis = state.get(HORIZONTAL_AXIS);

        // T and Cross
        Direction left = Direction.get(AxisDirection.POSITIVE, axis).rotateYClockwise();
        boolean onLeft = hasValidOutput(reader, pos.down(), left);
        boolean onRight = hasValidOutput(reader, pos.down(), left.getOpposite());

        if (onLeft && onRight)
            state = state.with(SHAPE, Shape.CROSS);
        else if (onLeft)
            state = state.with(SHAPE, Shape.T_LEFT);
        else if (onRight)
            state = state.with(SHAPE, Shape.T_RIGHT);

        if (state.get(SHAPE) == Shape.STRAIGHT) {
            boolean canHaveWindow = canHaveWindow(reader, pos, axis);
            if (canHaveWindow)
                state = state.with(SHAPE, Shape.WINDOW);
        }

        return state;
    }

    protected boolean canHaveWindow(BlockView reader, BlockPos pos, Axis axis) {
        Direction fw = Direction.get(AxisDirection.POSITIVE, axis);
        BlockState blockState1 = reader.getBlockState(pos.offset(fw));
        BlockState blockState2 = reader.getBlockState(pos.offset(fw.getOpposite()));

        boolean funnel1 = blockState1.getBlock() instanceof BeltFunnelBlock && blockState1.get(BeltFunnelBlock.SHAPE) == BeltFunnelBlock.Shape.EXTENDED && blockState1.get(
            BeltFunnelBlock.HORIZONTAL_FACING) == fw.getOpposite();
        boolean funnel2 = blockState2.getBlock() instanceof BeltFunnelBlock && blockState2.get(BeltFunnelBlock.SHAPE) == BeltFunnelBlock.Shape.EXTENDED && blockState2.get(
            BeltFunnelBlock.HORIZONTAL_FACING) == fw;

        boolean valid1 = blockState1.getBlock() instanceof BeltTunnelBlock || funnel1;
        boolean valid2 = blockState2.getBlock() instanceof BeltTunnelBlock || funnel2;
        boolean canHaveWindow = valid1 && valid2 && !(funnel1 && funnel2);
        return canHaveWindow;
    }

    private boolean hasValidOutput(BlockView world, BlockPos pos, Direction side) {
        BlockState blockState = world.getBlockState(pos.offset(side));
        if (blockState.isOf(AllBlocks.BELT))
            return blockState.get(BeltBlock.HORIZONTAL_FACING).getAxis() == side.getAxis();
        DirectBeltInputBehaviour behaviour = BlockEntityBehaviour.get(world, pos.offset(side), DirectBeltInputBehaviour.TYPE);
        return behaviour != null && behaviour.canInsertFromSide(side);
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        if (!hasWindow(state))
            return ActionResult.PASS;

        // Toggle windows
        Shape shape = state.get(SHAPE);
        shape = shape == Shape.CLOSED ? Shape.WINDOW : Shape.CLOSED;
        World world = context.getWorld();
        if (!world.isClient())
            world.setBlockState(context.getBlockPos(), state.with(SHAPE, shape), Block.NOTIFY_LISTENERS);
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        Direction fromAxis = Direction.get(AxisDirection.POSITIVE, state.get(HORIZONTAL_AXIS));
        Direction rotated = rotation.rotate(fromAxis);

        return state.with(HORIZONTAL_AXIS, rotated.getAxis());
    }

    @Override
    public void neighborUpdate(BlockState state, World worldIn, BlockPos pos, Block sourceBlock, BlockPos fromPos, boolean isMoving) {
        if (worldIn.isClient())
            return;

        if (fromPos.equals(pos.down())) {
            if (!canPlaceAt(state, worldIn, pos)) {
                worldIn.breakBlock(pos, true);
            }
        }
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_AXIS, SHAPE);
        super.appendProperties(builder);
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

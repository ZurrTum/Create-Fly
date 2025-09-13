package com.zurrtum.create.content.contraptions.elevator;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.zurrtum.create.content.contraptions.elevator.ElevatorColumn.ColumnCoords;
import com.zurrtum.create.content.redstone.contact.RedstoneContactBlock;
import com.zurrtum.create.content.redstone.diodes.BrassDiodeBlock;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.RedStoneConnectBlock;
import com.zurrtum.create.foundation.block.WeakPowerControlBlock;
import com.zurrtum.create.foundation.block.WrenchableDirectionalBlock;
import com.zurrtum.create.foundation.utility.BlockHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ElevatorContactBlock extends WrenchableDirectionalBlock implements IBE<ElevatorContactBlockEntity>, SpecialBlockItemRequirement, RedStoneConnectBlock, WeakPowerControlBlock {

    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final BooleanProperty CALLING = BooleanProperty.of("calling");
    public static final BooleanProperty POWERING = BrassDiodeBlock.POWERING;

    public static final MapCodec<ElevatorContactBlock> CODEC = createCodec(ElevatorContactBlock::new);

    public ElevatorContactBlock(Settings pProperties) {
        super(pProperties);
        setDefaultState(getDefaultState().with(CALLING, false).with(POWERING, false).with(POWERED, false).with(FACING, Direction.SOUTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(CALLING, POWERING, POWERED));
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        ActionResult onWrenched = super.onWrenched(state, context);
        if (onWrenched != ActionResult.SUCCESS)
            return onWrenched;

        World level = context.getWorld();
        if (level.isClient())
            return onWrenched;

        BlockPos pos = context.getBlockPos();
        state = level.getBlockState(pos);
        Direction facing = state.get(RedstoneContactBlock.FACING);
        if (facing.getAxis() != Axis.Y && ElevatorColumn.get(level, new ColumnCoords(pos.getX(), pos.getZ(), facing)) != null)
            return onWrenched;

        level.setBlockState(pos, BlockHelper.copyProperties(state, AllBlocks.REDSTONE_CONTACT.getDefaultState()));

        return onWrenched;
    }

    @Nullable
    public static ColumnCoords getColumnCoords(WorldAccess level, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);
        if (!blockState.isOf(AllBlocks.ELEVATOR_CONTACT) && !blockState.isOf(AllBlocks.REDSTONE_CONTACT))
            return null;
        Direction facing = blockState.get(FACING);
        BlockPos target = pos;
        return new ColumnCoords(target.getX(), target.getZ(), facing);
    }

    @Override
    public void neighborUpdate(
        BlockState pState,
        World pLevel,
        BlockPos pPos,
        Block pBlock,
        @Nullable WireOrientation wireOrientation,
        boolean pIsMoving
    ) {
        if (pLevel.isClient)
            return;

        boolean isPowered = pState.get(POWERED);
        if (isPowered == pLevel.isReceivingRedstonePower(pPos))
            return;

        pLevel.setBlockState(pPos, pState.cycle(POWERED), Block.NOTIFY_LISTENERS);

        if (isPowered)
            return;
        if (pState.get(CALLING))
            return;

        ElevatorColumn elevatorColumn = ElevatorColumn.getOrCreate(pLevel, getColumnCoords(pLevel, pPos));
        callToContactAndUpdate(elevatorColumn, pState, pLevel, pPos, true);
    }

    public void callToContactAndUpdate(ElevatorColumn elevatorColumn, BlockState pState, World pLevel, BlockPos pPos, boolean powered) {
        pLevel.setBlockState(pPos, pState.cycle(CALLING), Block.NOTIFY_LISTENERS);

        for (BlockPos otherPos : elevatorColumn.getContacts()) {
            if (otherPos.equals(pPos))
                continue;
            BlockState otherState = pLevel.getBlockState(otherPos);
            if (!otherState.isOf(AllBlocks.ELEVATOR_CONTACT))
                continue;
            pLevel.setBlockState(otherPos, otherState.with(CALLING, false), 2 | 16);
            scheduleActivation(pLevel, otherPos);
        }

        if (powered)
            pState = pState.with(POWERED, true);
        pLevel.setBlockState(pPos, pState.with(CALLING, true), Block.NOTIFY_LISTENERS);
        pLevel.updateNeighborsAlways(pPos, this, null);

        elevatorColumn.target(pPos.getY());
        elevatorColumn.markDirty();
    }

    public void scheduleActivation(ScheduledTickView pLevel, BlockPos pPos) {
        if (!pLevel.getBlockTickScheduler().isQueued(pPos, this))
            pLevel.scheduleBlockTick(pPos, this, 1);
    }

    @Override
    public void scheduledTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
        boolean wasPowering = pState.get(POWERING);

        Optional<ElevatorContactBlockEntity> optionalBE = getBlockEntityOptional(pLevel, pPos);
        boolean shouldBePowering = optionalBE.map(be -> {
            boolean activateBlock = be.activateBlock;
            be.activateBlock = false;
            be.markDirty();
            return activateBlock;
        }).orElse(false);

        shouldBePowering |= RedstoneContactBlock.hasValidContact(pLevel, pPos, pState.get(FACING));

        if (wasPowering || shouldBePowering)
            pLevel.setBlockState(pPos, pState.with(POWERING, shouldBePowering), 2 | 16);

        pLevel.updateNeighborsAlways(pPos, this, null);
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState stateIn,
        WorldView worldIn,
        ScheduledTickView tickView,
        BlockPos currentPos,
        Direction facing,
        BlockPos facingPos,
        BlockState facingState,
        Random random
    ) {
        if (facing != stateIn.get(FACING))
            return stateIn;
        boolean hasValidContact = RedstoneContactBlock.hasValidContact(worldIn, currentPos, facing);
        if (stateIn.get(POWERING) != hasValidContact)
            scheduleActivation(tickView, currentPos);
        return stateIn;
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, RedstoneView level, BlockPos pos, Direction side) {
        return false;
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return state.get(POWERING);
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return AllItems.REDSTONE_CONTACT.getDefaultStack();
    }

    @Override
    public boolean canConnectRedstone(BlockState state, @Nullable Direction side) {
        if (side == null)
            return true;
        return state.get(FACING) != side.getOpposite();
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView blockAccess, BlockPos pos, Direction side) {
        if (side == null)
            return 0;
        BlockState toState = blockAccess.getBlockState(pos.offset(side.getOpposite()));
        if (toState.isOf(this))
            return 0;
        return state.get(POWERING) ? 15 : 0;
    }

    @Override
    public Class<ElevatorContactBlockEntity> getBlockEntityClass() {
        return ElevatorContactBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ElevatorContactBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.ELEVATOR_CONTACT;
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity be) {
        return ItemRequirement.of(AllBlocks.REDSTONE_CONTACT.getDefaultState(), be);
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
        if (player != null && stack.isOf(AllItems.WRENCH))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (level.isClient) {
            withBlockEntityDo(
                level, pos, be -> {
                    AllClientHandle.INSTANCE.openElevatorContactScreen(be, player);
                }
            );
        }
        return ActionResult.SUCCESS;
    }

    public static int getLight(BlockState state) {
        return state.get(POWERING) ? 10 : 0;
    }

    @Override
    protected @NotNull MapCodec<? extends FacingBlock> getCodec() {
        return CODEC;
    }
}

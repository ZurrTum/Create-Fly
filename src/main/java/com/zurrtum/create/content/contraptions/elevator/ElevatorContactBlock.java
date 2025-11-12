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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ElevatorContactBlock extends WrenchableDirectionalBlock implements IBE<ElevatorContactBlockEntity>, SpecialBlockItemRequirement, RedStoneConnectBlock, WeakPowerControlBlock {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty CALLING = BooleanProperty.create("calling");
    public static final BooleanProperty POWERING = BrassDiodeBlock.POWERING;

    public static final MapCodec<ElevatorContactBlock> CODEC = simpleCodec(ElevatorContactBlock::new);

    public ElevatorContactBlock(Properties pProperties) {
        super(pProperties);
        registerDefaultState(defaultBlockState().setValue(CALLING, false).setValue(POWERING, false).setValue(POWERED, false).setValue(FACING, Direction.SOUTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(CALLING, POWERING, POWERED));
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        InteractionResult onWrenched = super.onWrenched(state, context);
        if (onWrenched != InteractionResult.SUCCESS)
            return onWrenched;

        Level level = context.getLevel();
        if (level.isClientSide())
            return onWrenched;

        BlockPos pos = context.getClickedPos();
        state = level.getBlockState(pos);
        Direction facing = state.getValue(RedstoneContactBlock.FACING);
        if (facing.getAxis() != Axis.Y && ElevatorColumn.get(level, new ColumnCoords(pos.getX(), pos.getZ(), facing)) != null)
            return onWrenched;

        level.setBlockAndUpdate(pos, BlockHelper.copyProperties(state, AllBlocks.REDSTONE_CONTACT.defaultBlockState()));

        return onWrenched;
    }

    @Nullable
    public static ColumnCoords getColumnCoords(LevelAccessor level, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);
        if (!blockState.is(AllBlocks.ELEVATOR_CONTACT) && !blockState.is(AllBlocks.REDSTONE_CONTACT))
            return null;
        Direction facing = blockState.getValue(FACING);
        BlockPos target = pos;
        return new ColumnCoords(target.getX(), target.getZ(), facing);
    }

    @Override
    public void neighborChanged(
        BlockState pState,
        Level pLevel,
        BlockPos pPos,
        Block pBlock,
        @Nullable Orientation wireOrientation,
        boolean pIsMoving
    ) {
        if (pLevel.isClientSide())
            return;

        boolean isPowered = pState.getValue(POWERED);
        if (isPowered == pLevel.hasNeighborSignal(pPos))
            return;

        pLevel.setBlock(pPos, pState.cycle(POWERED), Block.UPDATE_CLIENTS);

        if (isPowered)
            return;
        if (pState.getValue(CALLING))
            return;

        ElevatorColumn elevatorColumn = ElevatorColumn.getOrCreate(pLevel, getColumnCoords(pLevel, pPos));
        callToContactAndUpdate(elevatorColumn, pState, pLevel, pPos, true);
    }

    public void callToContactAndUpdate(ElevatorColumn elevatorColumn, BlockState pState, Level pLevel, BlockPos pPos, boolean powered) {
        pLevel.setBlock(pPos, pState.cycle(CALLING), Block.UPDATE_CLIENTS);

        for (BlockPos otherPos : elevatorColumn.getContacts()) {
            if (otherPos.equals(pPos))
                continue;
            BlockState otherState = pLevel.getBlockState(otherPos);
            if (!otherState.is(AllBlocks.ELEVATOR_CONTACT))
                continue;
            pLevel.setBlock(otherPos, otherState.setValue(CALLING, false), 2 | 16);
            scheduleActivation(pLevel, otherPos);
        }

        if (powered)
            pState = pState.setValue(POWERED, true);
        pLevel.setBlock(pPos, pState.setValue(CALLING, true), Block.UPDATE_CLIENTS);
        pLevel.updateNeighborsAt(pPos, this, null);

        elevatorColumn.target(pPos.getY());
        elevatorColumn.markDirty();
    }

    public void scheduleActivation(ScheduledTickAccess pLevel, BlockPos pPos) {
        if (!pLevel.getBlockTicks().hasScheduledTick(pPos, this))
            pLevel.scheduleTick(pPos, this, 1);
    }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRand) {
        boolean wasPowering = pState.getValue(POWERING);

        Optional<ElevatorContactBlockEntity> optionalBE = getBlockEntityOptional(pLevel, pPos);
        boolean shouldBePowering = optionalBE.map(be -> {
            boolean activateBlock = be.activateBlock;
            be.activateBlock = false;
            be.setChanged();
            return activateBlock;
        }).orElse(false);

        shouldBePowering |= RedstoneContactBlock.hasValidContact(pLevel, pPos, pState.getValue(FACING));

        if (wasPowering || shouldBePowering)
            pLevel.setBlock(pPos, pState.setValue(POWERING, shouldBePowering), 2 | 16);

        pLevel.updateNeighborsAt(pPos, this, null);
    }

    @Override
    public BlockState updateShape(
        BlockState stateIn,
        LevelReader worldIn,
        ScheduledTickAccess tickView,
        BlockPos currentPos,
        Direction facing,
        BlockPos facingPos,
        BlockState facingState,
        RandomSource random
    ) {
        if (facing != stateIn.getValue(FACING))
            return stateIn;
        boolean hasValidContact = RedstoneContactBlock.hasValidContact(worldIn, currentPos, facing);
        if (stateIn.getValue(POWERING) != hasValidContact)
            scheduleActivation(tickView, currentPos);
        return stateIn;
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, SignalGetter level, BlockPos pos, Direction side) {
        return false;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return state.getValue(POWERING);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
        return AllItems.REDSTONE_CONTACT.getDefaultInstance();
    }

    @Override
    public boolean canConnectRedstone(BlockState state, @Nullable Direction side) {
        if (side == null)
            return true;
        return state.getValue(FACING) != side.getOpposite();
    }

    @Override
    public int getSignal(BlockState state, BlockGetter blockAccess, BlockPos pos, Direction side) {
        if (side == null)
            return 0;
        BlockState toState = blockAccess.getBlockState(pos.relative(side.getOpposite()));
        if (toState.is(this))
            return 0;
        return state.getValue(POWERING) ? 15 : 0;
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
        return ItemRequirement.of(AllBlocks.REDSTONE_CONTACT.defaultBlockState(), be);
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
        if (player != null && stack.is(AllItems.WRENCH))
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide()) {
            withBlockEntityDo(
                level, pos, be -> {
                    AllClientHandle.INSTANCE.openElevatorContactScreen(be, player);
                }
            );
        }
        return InteractionResult.SUCCESS;
    }

    public static int getLight(BlockState state) {
        return state.getValue(POWERING) ? 10 : 0;
    }

    @Override
    protected @NotNull MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }
}

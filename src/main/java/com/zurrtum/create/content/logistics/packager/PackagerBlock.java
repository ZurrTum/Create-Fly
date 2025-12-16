package com.zurrtum.create.content.logistics.packager;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.block.*;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class PackagerBlock extends WrenchableDirectionalBlock implements IBE<PackagerBlockEntity>, IWrenchable, ItemInventoryProvider<PackagerBlockEntity>, NeighborChangeListeningBlock, WeakPowerControlBlock, NeighborUpdateListeningBlock {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty LINKED = BooleanProperty.create("linked");

    public PackagerBlock(Properties properties) {
        super(properties);
        BlockState defaultBlockState = defaultBlockState();
        if (defaultBlockState.hasProperty(LINKED))
            defaultBlockState = defaultBlockState.setValue(LINKED, false);
        registerDefaultState(defaultBlockState.setValue(POWERED, false));
    }

    @Override
    public Container getInventory(LevelAccessor world, BlockPos pos, BlockState state, PackagerBlockEntity blockEntity, Direction context) {
        return blockEntity.inventory;
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferredFacing = null;
        for (Direction face : context.getNearestLookingDirections()) {
            BlockPos pos = context.getClickedPos().relative(face);
            BlockEntity be = context.getLevel().getBlockEntity(pos);
            if (be instanceof PackagerBlockEntity)
                continue;
            if (be != null && be.hasLevel() && ItemHelper.getInventory(be.getLevel(), pos, null, be, null) != null) {
                preferredFacing = face.getOpposite();
                break;
            }
        }

        Player player = context.getPlayer();
        if (preferredFacing == null) {
            Direction facing = context.getNearestLookingDirection();
            preferredFacing = player != null && player.isShiftKeyDown() ? facing : facing.getOpposite();
        }

        if (player != null && !(FakePlayerHandler.has(player))) {
            if (context.getLevel().getBlockState(context.getClickedPos().relative(preferredFacing.getOpposite()))
                .is(AllBlocks.PORTABLE_STORAGE_INTERFACE)) {
                player.displayClientMessage(Component.translatable("create.packager.no_portable_storage"), true);
                return null;
            }
        }

        return super.getStateForPlacement(context).setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()))
            .setValue(FACING, preferredFacing);
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
        if (stack.is(AllItems.WRENCH))
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (stack.is(AllItems.FACTORY_GAUGE))
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (stack.is(AllItems.STOCK_LINK) && !(state.hasProperty(LINKED) && state.getValue(LINKED)))
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (stack.is(AllItems.PACKAGE_FROGPORT))
            return InteractionResult.TRY_WITH_EMPTY_HAND;

        if (onBlockEntityUseItemOn(
            level, pos, be -> {
                if (be.heldBox.isEmpty()) {
                    if (be.animationTicks > 0)
                        return InteractionResult.SUCCESS;
                    if (PackageItem.isPackage(stack)) {
                        if (level.isClientSide())
                            return InteractionResult.SUCCESS;
                        if (!be.unwrapBox(stack.copy(), true))
                            return InteractionResult.SUCCESS;
                        be.unwrapBox(stack.copy(), false);
                        be.triggerStockCheck();
                        stack.shrink(1);
                        AllSoundEvents.DEPOT_PLOP.playOnServer(level, pos);
                        if (stack.isEmpty())
                            player.setItemInHand(hand, ItemStack.EMPTY);
                        return InteractionResult.SUCCESS;
                    }
                    return InteractionResult.SUCCESS;
                }
                if (be.animationTicks > 0)
                    return InteractionResult.SUCCESS;
                if (!level.isClientSide()) {
                    player.getInventory().placeItemBackInInventory(be.heldBox.copy());
                    player.level().playSound(
                        null,
                        player.blockPosition(),
                        SoundEvents.ITEM_PICKUP,
                        SoundSource.PLAYERS,
                        .2f,
                        1f + player.level().getRandom().nextFloat()
                    );
                    be.heldBox = ItemStack.EMPTY;
                    be.notifyUpdate();
                }
                return InteractionResult.SUCCESS;
            }
        ).consumesAction())
            return InteractionResult.SUCCESS;

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(POWERED, LINKED));
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        if (neighbor.relative(state.getValueOrElse(FACING, Direction.UP)).equals(pos))
            withBlockEntityDo(level, pos, PackagerBlockEntity::triggerStockCheck);
    }

    @Override
    public void neighborUpdate(BlockState state, Level worldIn, BlockPos pos, Block sourceBlock, BlockPos fromPos, boolean isMoving) {
        if (worldIn.isClientSide())
            return;
        InvManipulationBehaviour behaviour = BlockEntityBehaviour.get(worldIn, pos, InvManipulationBehaviour.TYPE);
        if (behaviour != null)
            behaviour.onNeighborChanged(fromPos);
    }

    @Override
    public void neighborChanged(
        BlockState state,
        Level worldIn,
        BlockPos pos,
        Block blockIn,
        @Nullable Orientation wireOrientation,
        boolean isMoving
    ) {
        if (worldIn.isClientSide())
            return;
        boolean previouslyPowered = state.getValue(POWERED);
        if (previouslyPowered == worldIn.hasNeighborSignal(pos))
            return;
        worldIn.setBlock(pos, state.cycle(POWERED), Block.UPDATE_CLIENTS);
        if (!previouslyPowered)
            withBlockEntityDo(worldIn, pos, PackagerBlockEntity::activate);
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, SignalGetter level, BlockPos pos, Direction side) {
        return false;
    }

    @Override
    public Class<PackagerBlockEntity> getBlockEntityClass() {
        return PackagerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PackagerBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.PACKAGER;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState pState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos, Direction direction) {
        return getBlockEntityOptional(pLevel, pPos).map(pbe -> {
            boolean empty = pbe.inventory.getStack().isEmpty();
            if (pbe.animationTicks != 0)
                empty = false;
            return empty ? 0 : 15;
        }).orElse(0);
    }

}

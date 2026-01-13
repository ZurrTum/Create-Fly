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
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RedstoneView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

public class PackagerBlock extends WrenchableDirectionalBlock implements IBE<PackagerBlockEntity>, IWrenchable, ItemInventoryProvider<PackagerBlockEntity>, NeighborChangeListeningBlock, WeakPowerControlBlock, NeighborUpdateListeningBlock {

    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final BooleanProperty LINKED = BooleanProperty.of("linked");

    public PackagerBlock(Settings properties) {
        super(properties);
        BlockState defaultBlockState = getDefaultState();
        if (defaultBlockState.contains(LINKED))
            defaultBlockState = defaultBlockState.with(LINKED, false);
        setDefaultState(defaultBlockState.with(POWERED, false));
    }

    @Override
    public Inventory getInventory(WorldAccess world, BlockPos pos, BlockState state, PackagerBlockEntity blockEntity, Direction context) {
        return blockEntity.inventory;
    }

    @Override
    public void onPlaced(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.onPlaced(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Direction preferredFacing = null;
        for (Direction face : context.getPlacementDirections()) {
            BlockPos pos = context.getBlockPos().offset(face);
            BlockEntity be = context.getWorld().getBlockEntity(pos);
            if (be instanceof PackagerBlockEntity)
                continue;
            if (be != null && be.hasWorld() && ItemHelper.getInventory(be.getWorld(), pos, null, be, null) != null) {
                preferredFacing = face.getOpposite();
                break;
            }
        }

        PlayerEntity player = context.getPlayer();
        if (preferredFacing == null) {
            Direction facing = context.getPlayerLookDirection();
            preferredFacing = player != null && player.isSneaking() ? facing : facing.getOpposite();
        }

        if (player != null && !(FakePlayerHandler.has(player))) {
            if (context.getWorld().getBlockState(context.getBlockPos().offset(preferredFacing.getOpposite()))
                .isOf(AllBlocks.PORTABLE_STORAGE_INTERFACE)) {
                player.sendMessage(Text.translatable("create.packager.no_portable_storage"), true);
                return null;
            }
        }

        return super.getPlacementState(context).with(POWERED, context.getWorld().isReceivingRedstonePower(context.getBlockPos()))
            .with(FACING, preferredFacing);
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
        if (stack.isOf(AllItems.WRENCH))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (stack.isOf(AllItems.FACTORY_GAUGE))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (stack.isOf(AllItems.STOCK_LINK) && !(state.contains(LINKED) && state.get(LINKED)))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (stack.isOf(AllItems.PACKAGE_FROGPORT))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        if (onBlockEntityUseItemOn(
            level, pos, be -> {
                if (be.heldBox.isEmpty()) {
                    if (be.animationTicks > 0)
                        return ActionResult.SUCCESS;
                    if (PackageItem.isPackage(stack)) {
                        if (level.isClient())
                            return ActionResult.SUCCESS;
                        if (!be.unwrapBox(stack.copy(), true))
                            return ActionResult.SUCCESS;
                        be.unwrapBox(stack.copy(), false);
                        be.triggerStockCheck();
                        stack.decrement(1);
                        AllSoundEvents.DEPOT_PLOP.playOnServer(level, pos);
                        if (stack.isEmpty())
                            player.setStackInHand(hand, ItemStack.EMPTY);
                        return ActionResult.SUCCESS;
                    }
                    return ActionResult.SUCCESS;
                }
                if (be.animationTicks > 0)
                    return ActionResult.SUCCESS;
                if (!level.isClient()) {
                    player.getInventory().offerOrDrop(be.heldBox.copy());
                    player.getEntityWorld().playSound(
                        null,
                        player.getBlockPos(),
                        SoundEvents.ENTITY_ITEM_PICKUP,
                        SoundCategory.PLAYERS,
                        .2f,
                        1f + player.getEntityWorld().random.nextFloat()
                    );
                    be.heldBox = ItemStack.EMPTY;
                    be.notifyUpdate();
                }
                return ActionResult.SUCCESS;
            }
        ).isAccepted())
            return ActionResult.SUCCESS;

        return ActionResult.SUCCESS;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(POWERED, LINKED));
    }

    @Override
    public void onNeighborChange(BlockState state, WorldView level, BlockPos pos, BlockPos neighbor) {
        if (neighbor.offset(state.get(FACING, Direction.UP)).equals(pos))
            withBlockEntityDo(level, pos, PackagerBlockEntity::triggerStockCheck);
    }

    @Override
    public void neighborUpdate(BlockState state, World worldIn, BlockPos pos, Block sourceBlock, BlockPos fromPos, boolean isMoving) {
        if (worldIn.isClient())
            return;
        InvManipulationBehaviour behaviour = BlockEntityBehaviour.get(worldIn, pos, InvManipulationBehaviour.TYPE);
        if (behaviour != null)
            behaviour.onNeighborChanged(fromPos);
    }

    @Override
    public void neighborUpdate(
        BlockState state,
        World worldIn,
        BlockPos pos,
        Block blockIn,
        @Nullable WireOrientation wireOrientation,
        boolean isMoving
    ) {
        if (worldIn.isClient())
            return;
        boolean previouslyPowered = state.get(POWERED);
        if (previouslyPowered == worldIn.isReceivingRedstonePower(pos))
            return;
        worldIn.setBlockState(pos, state.cycle(POWERED), Block.NOTIFY_LISTENERS);
        if (!previouslyPowered)
            withBlockEntityDo(worldIn, pos, PackagerBlockEntity::activate);
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, RedstoneView level, BlockPos pos, Direction side) {
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
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    @Override
    public boolean hasComparatorOutput(BlockState pState) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState pState, World pLevel, BlockPos pPos, Direction direction) {
        return getBlockEntityOptional(pLevel, pPos).map(pbe -> {
            boolean empty = pbe.inventory.getStack().isEmpty();
            if (pbe.animationTicks != 0)
                empty = false;
            return empty ? 0 : 15;
        }).orElse(0);
    }

}

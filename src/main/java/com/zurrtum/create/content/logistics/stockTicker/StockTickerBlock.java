package com.zurrtum.create.content.logistics.stockTicker;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerBlockEntity.StockTickerInventory;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.gui.menu.MenuProvider;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StockTickerBlock extends HorizontalDirectionalBlock implements IBE<StockTickerBlockEntity>, IWrenchable, ItemInventoryProvider<StockTickerBlockEntity> {

    public static final MapCodec<StockTickerBlock> CODEC = simpleCodec(StockTickerBlock::new);

    public StockTickerBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Container getInventory(LevelAccessor world, BlockPos pos, BlockState state, StockTickerBlockEntity blockEntity, Direction context) {
        return blockEntity.receivedPayments;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        Direction facing = pContext.getHorizontalDirection().getOpposite();
        boolean reverse = pContext.getPlayer() != null && pContext.getPlayer().isShiftKeyDown();
        return super.getStateForPlacement(pContext).setValue(FACING, reverse ? facing.getOpposite() : facing);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(FACING));
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
        if (stack.getItem() instanceof LogisticallyLinkedBlockItem)
            return InteractionResult.TRY_WITH_EMPTY_HAND;

        return onBlockEntityUseItemOn(
            level, pos, stbe -> {
                if (!stbe.behaviour.mayInteractMessage(player))
                    return InteractionResult.SUCCESS;

                if (!level.isClientSide()) {
                    StockTickerInventory inventory = stbe.receivedPayments;
                    Inventory playerInventory = player.getInventory();
                    boolean anySuccess = false;
                    for (int i = 0, size = inventory.getContainerSize(); i < size; i++) {
                        ItemStack target = inventory.getItem(i);
                        if (target.isEmpty()) {
                            continue;
                        }
                        inventory.setItem(i, ItemStack.EMPTY);
                        playerInventory.placeItemBackInInventory(target);
                        anySuccess = true;
                    }
                    if (anySuccess) {
                        inventory.setChanged();
                        player.level().playSound(
                            null,
                            player.blockPosition(),
                            SoundEvents.ITEM_PICKUP,
                            SoundSource.PLAYERS,
                            .2f,
                            1f + player.level().random.nextFloat()
                        );
                        return InteractionResult.SUCCESS;
                    }
                }

                if (player instanceof ServerPlayer sp) {
                    if (stbe.isKeeperPresent())
                        MenuProvider.openHandledScreen(sp, stbe::createCategoryMenu);
                    else
                        player.displayClientMessage(Component.translatable("create.stock_ticker.keeper_missing"), true);
                }

                return InteractionResult.SUCCESS;
            }
        );
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return AllShapes.STOCK_TICKER;
    }

    @Override
    public Class<StockTickerBlockEntity> getBlockEntityClass() {
        return StockTickerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends StockTickerBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.STOCK_TICKER;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }
}

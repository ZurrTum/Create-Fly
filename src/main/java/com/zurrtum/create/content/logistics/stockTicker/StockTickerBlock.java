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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class StockTickerBlock extends HorizontalFacingBlock implements IBE<StockTickerBlockEntity>, IWrenchable, ItemInventoryProvider<StockTickerBlockEntity> {

    public static final MapCodec<StockTickerBlock> CODEC = createCodec(StockTickerBlock::new);

    public StockTickerBlock(Settings pProperties) {
        super(pProperties);
    }

    @Override
    public Inventory getInventory(WorldAccess world, BlockPos pos, BlockState state, StockTickerBlockEntity blockEntity, Direction context) {
        return blockEntity.receivedPayments;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        Direction facing = pContext.getHorizontalPlayerFacing().getOpposite();
        boolean reverse = pContext.getPlayer() != null && pContext.getPlayer().isSneaking();
        return super.getPlacementState(pContext).with(FACING, reverse ? facing.getOpposite() : facing);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> pBuilder) {
        super.appendProperties(pBuilder.add(FACING));
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
        if (stack.getItem() instanceof LogisticallyLinkedBlockItem)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        return onBlockEntityUseItemOn(
            level, pos, stbe -> {
                if (!stbe.behaviour.mayInteractMessage(player))
                    return ActionResult.SUCCESS;

                if (!level.isClient()) {
                    StockTickerInventory inventory = stbe.receivedPayments;
                    PlayerInventory playerInventory = player.getInventory();
                    boolean anySuccess = false;
                    for (int i = 0, size = inventory.size(); i < size; i++) {
                        ItemStack target = inventory.getStack(i);
                        if (target.isEmpty()) {
                            continue;
                        }
                        inventory.setStack(i, ItemStack.EMPTY);
                        playerInventory.offerOrDrop(target);
                        anySuccess = true;
                    }
                    if (anySuccess) {
                        inventory.markDirty();
                        player.getEntityWorld().playSound(
                            null,
                            player.getBlockPos(),
                            SoundEvents.ENTITY_ITEM_PICKUP,
                            SoundCategory.PLAYERS,
                            .2f,
                            1f + player.getEntityWorld().random.nextFloat()
                        );
                        return ActionResult.SUCCESS;
                    }
                }

                if (player instanceof ServerPlayerEntity sp) {
                    if (stbe.isKeeperPresent())
                        MenuProvider.openHandledScreen(sp, stbe::createCategoryMenu);
                    else
                        player.sendMessage(Text.translatable("create.stock_ticker.keeper_missing"), true);
                }

                return ActionResult.SUCCESS;
            }
        );
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
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
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }
}

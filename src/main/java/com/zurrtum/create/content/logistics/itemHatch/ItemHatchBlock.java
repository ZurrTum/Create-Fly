package com.zurrtum.create.content.logistics.itemHatch;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.item.ItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ItemHatchBlock extends HorizontalDirectionalBlock implements IBE<ItemHatchBlockEntity>, IWrenchable, ProperWaterloggedBlock {
    public static final MapCodec<ItemHatchBlock> CODEC = simpleCodec(ItemHatchBlock::new);

    public static final BooleanProperty OPEN = BooleanProperty.create("open");

    public ItemHatchBlock(Properties pProperties) {
        super(pProperties);
        registerDefaultState(defaultBlockState().setValue(OPEN, false).setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(OPEN, FACING, WATERLOGGED));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockState state = super.getStateForPlacement(pContext);
        if (state == null)
            return state;
        if (pContext.getClickedFace().getAxis().isVertical())
            return null;

        return withWater(state.setValue(FACING, pContext.getClickedFace().getOpposite()).setValue(OPEN, false), pContext);
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return fluidState(pState);
    }

    @Override
    public BlockState updateShape(
        BlockState pState,
        LevelReader pLevel,
        ScheduledTickAccess tickView,
        BlockPos pPos,
        Direction pDirection,
        BlockPos pNeighborPos,
        BlockState pNeighborState,
        RandomSource random
    ) {
        updateWater(pLevel, tickView, pState, pPos);
        return pState;
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
        if (level.isClientSide())
            return InteractionResult.SUCCESS;
        if (FakePlayerHandler.has(player))
            return InteractionResult.SUCCESS;

        BlockEntity blockEntity = level.getBlockEntity(pos.relative(state.getValue(FACING)));
        if (blockEntity == null)
            return InteractionResult.FAIL;
        Container targetInv = ItemHelper.getInventory(level, blockEntity.getBlockPos(), null, blockEntity, null);
        if (targetInv == null)
            return InteractionResult.FAIL;

        ServerFilteringBehaviour filter = BlockEntityBehaviour.get(level, pos, ServerFilteringBehaviour.TYPE);
        if (filter == null)
            return InteractionResult.FAIL;

        Inventory inventory = player.getInventory();
        boolean anyInserted = false;
        boolean depositItemInHand = !player.isShiftKeyDown();

        if (!depositItemInHand && stack.is(AllItemTags.TOOLS_WRENCH))
            return InteractionResult.TRY_WITH_EMPTY_HAND;

        int start, end;
        if (depositItemInHand) {
            start = end = inventory.getSelectedSlot();
        } else {
            start = Inventory.getSelectionSize();
            end = Inventory.INVENTORY_SIZE - 1;
        }
        for (int i = start; i <= end; i++) {
            ItemStack item = inventory.getItem(i);
            if (item.isEmpty())
                continue;
            if (!item.getItem().canFitInsideContainerItems() && !PackageItem.isPackage(item))
                continue;
            if (!filter.getFilter().isEmpty() && !filter.test(item))
                continue;

            int count = item.getCount();
            int insert = targetInv.insertExist(item, count);
            if (insert == 0) {
                continue;
            }
            anyInserted = true;
            if (insert == count) {
                inventory.setItem(i, ItemStack.EMPTY);
            } else {
                inventory.setItem(i, item.copyWithCount(count - insert));
            }
        }

        if (!anyInserted)
            return InteractionResult.SUCCESS;

        AllSoundEvents.ITEM_HATCH.playOnServer(level, pos);
        level.setBlockAndUpdate(pos, state.setValue(OPEN, true));
        level.scheduleTick(pos, this, 10);

        player.displayClientMessage(Component.translatable(depositItemInHand ? "create.item_hatch.deposit_item" : "create.item_hatch.deposit_inventory"), true);
        return InteractionResult.SUCCESS;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return AllShapes.ITEM_HATCH.get(pState.getValue(FACING).getOpposite());
    }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        if (pState.getValue(OPEN))
            pLevel.setBlockAndUpdate(pPos, pState.setValue(OPEN, false));
    }

    @Override
    public Class<ItemHatchBlockEntity> getBlockEntityClass() {
        return ItemHatchBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ItemHatchBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.ITEM_HATCH;
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
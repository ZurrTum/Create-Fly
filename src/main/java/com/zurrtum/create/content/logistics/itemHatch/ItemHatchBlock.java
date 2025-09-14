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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class ItemHatchBlock extends HorizontalFacingBlock implements IBE<ItemHatchBlockEntity>, IWrenchable, ProperWaterloggedBlock {
    public static final MapCodec<ItemHatchBlock> CODEC = createCodec(ItemHatchBlock::new);

    public static final BooleanProperty OPEN = BooleanProperty.of("open");

    public ItemHatchBlock(Settings pProperties) {
        super(pProperties);
        setDefaultState(getDefaultState().with(OPEN, false).with(WATERLOGGED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> pBuilder) {
        super.appendProperties(pBuilder.add(OPEN, FACING, WATERLOGGED));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        BlockState state = super.getPlacementState(pContext);
        if (state == null)
            return state;
        if (pContext.getSide().getAxis().isVertical())
            return null;

        return withWater(state.with(FACING, pContext.getSide().getOpposite()).with(OPEN, false), pContext);
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return fluidState(pState);
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState pState,
        WorldView pLevel,
        ScheduledTickView tickView,
        BlockPos pPos,
        Direction pDirection,
        BlockPos pNeighborPos,
        BlockState pNeighborState,
        Random random
    ) {
        updateWater(pLevel, tickView, pState, pPos);
        return pState;
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
        if (level.isClient())
            return ActionResult.SUCCESS;
        if (FakePlayerHandler.has(player))
            return ActionResult.SUCCESS;

        BlockEntity blockEntity = level.getBlockEntity(pos.offset(state.get(FACING)));
        if (blockEntity == null)
            return ActionResult.FAIL;
        Inventory targetInv = ItemHelper.getInventory(level, blockEntity.getPos(), null, blockEntity, null);
        if (targetInv == null)
            return ActionResult.FAIL;

        ServerFilteringBehaviour filter = BlockEntityBehaviour.get(level, pos, ServerFilteringBehaviour.TYPE);
        if (filter == null)
            return ActionResult.FAIL;

        PlayerInventory inventory = player.getInventory();
        boolean anyInserted = false;
        boolean depositItemInHand = !player.isSneaking();

        if (!depositItemInHand && stack.isIn(AllItemTags.TOOLS_WRENCH))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        int start, end;
        if (depositItemInHand) {
            start = end = inventory.getSelectedSlot();
        } else {
            start = PlayerInventory.getHotbarSize();
            end = PlayerInventory.MAIN_SIZE - 1;
        }
        for (int i = start; i <= end; i++) {
            ItemStack item = inventory.getStack(i);
            if (item.isEmpty())
                continue;
            if (!item.getItem().canBeNested() && !PackageItem.isPackage(item))
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
                inventory.setStack(i, ItemStack.EMPTY);
            } else {
                inventory.setStack(i, item.copyWithCount(count - insert));
            }
        }

        if (!anyInserted)
            return ActionResult.SUCCESS;

        AllSoundEvents.ITEM_HATCH.playOnServer(level, pos);
        level.setBlockState(pos, state.with(OPEN, true));
        level.scheduleBlockTick(pos, this, 10);

        player.sendMessage(Text.translatable(depositItemInHand ? "create.item_hatch.deposit_item" : "create.item_hatch.deposit_inventory"), true);
        return ActionResult.SUCCESS;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return AllShapes.ITEM_HATCH.get(pState.get(FACING).getOpposite());
    }

    @Override
    public void scheduledTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom) {
        if (pState.get(OPEN))
            pLevel.setBlockState(pPos, pState.with(OPEN, false));
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
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }
}
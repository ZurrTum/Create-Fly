package com.zurrtum.create.content.trains.station;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.logistics.depot.SharedDepotBlockMethods;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class StationBlock extends Block implements IBE<StationBlockEntity>, ItemInventoryProvider<StationBlockEntity>, IWrenchable, ProperWaterloggedBlock {

    public static final BooleanProperty ASSEMBLING = BooleanProperty.create("assembling");

    public StationBlock(Properties p_54120_) {
        super(p_54120_);
        registerDefaultState(defaultBlockState().setValue(ASSEMBLING, false).setValue(WATERLOGGED, false));
    }

    @Override
    public Container getInventory(LevelAccessor world, BlockPos pos, BlockState state, StationBlockEntity blockEntity, @Nullable Direction context) {
        return blockEntity.depotBehaviour.itemHandler;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(ASSEMBLING, WATERLOGGED));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return withWater(super.getStateForPlacement(pContext), pContext);
    }

    @Override
    public BlockState updateShape(
        BlockState pState,
        LevelReader pLevel,
        ScheduledTickAccess tickView,
        BlockPos pCurrentPos,
        Direction pDirection,
        BlockPos pNeighborPos,
        BlockState pNeighborState,
        RandomSource random
    ) {
        updateWater(pLevel, tickView, pState, pCurrentPos);
        return pState;
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return fluidState(pState);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState pState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos, Direction direction) {
        return getBlockEntityOptional(pLevel, pPos).map(ste -> ste.trainPresent ? 15 : 0).orElse(0);
    }

    @Override
    public void updateEntityMovementAfterFallOn(BlockGetter worldIn, Entity entityIn) {
        super.updateEntityMovementAfterFallOn(worldIn, entityIn);
        SharedDepotBlockMethods.onLanded(worldIn, entityIn);
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
        if (player == null || player.isShiftKeyDown())
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (stack.is(AllItems.WRENCH))
            return InteractionResult.TRY_WITH_EMPTY_HAND;

        if (stack.getItem() == Items.FILLED_MAP) {
            return onBlockEntityUseItemOn(
                level, pos, station -> {
                    if (level.isClientSide())
                        return InteractionResult.SUCCESS;

                    if (station.getStation() == null || station.getStation().getId() == null)
                        return InteractionResult.FAIL;

                    MapItemSavedData savedData = MapItem.getSavedData(stack, level);
                    if (!(savedData instanceof StationMapData stationMapData))
                        return InteractionResult.FAIL;

                    if (!stationMapData.create$toggleStation(level, pos, station))
                        return InteractionResult.FAIL;

                    return InteractionResult.SUCCESS;
                }
            );
        }

        InteractionResult result = onBlockEntityUse(
            level, pos, station -> {
                ItemStack autoSchedule = station.getAutoSchedule();
                if (autoSchedule.isEmpty())
                    return InteractionResult.PASS;
                if (level.isClientSide())
                    return InteractionResult.SUCCESS;
                player.getInventory().placeItemBackInInventory(autoSchedule.copy());
                station.depotBehaviour.removeHeldItem();
                station.notifyUpdate();
                player.level().playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.ITEM_PICKUP,
                    SoundSource.PLAYERS,
                    .2f,
                    1f + player.level().getRandom().nextFloat()
                );
                return InteractionResult.SUCCESS;
            }
        );

        if (result == InteractionResult.PASS)
            AllClientHandle.INSTANCE.openStationScreen(level, pos, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return AllShapes.STATION;
    }

    @Override
    public Class<StationBlockEntity> getBlockEntityClass() {
        return StationBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends StationBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.TRACK_STATION;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

}

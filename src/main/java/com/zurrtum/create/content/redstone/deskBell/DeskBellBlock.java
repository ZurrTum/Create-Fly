package com.zurrtum.create.content.redstone.deskBell;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import com.zurrtum.create.foundation.block.WrenchableDirectionalBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class DeskBellBlock extends WrenchableDirectionalBlock implements ProperWaterloggedBlock, IBE<DeskBellBlockEntity> {

    public static final BooleanProperty POWERED = Properties.POWERED;

    public DeskBellBlock(Settings properties) {
        super(properties);
        setDefaultState(getDefaultState().with(FACING, Direction.UP).with(POWERED, false).with(WATERLOGGED, false));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return withWater(getDefaultState().with(FACING, context.getSide()), context);
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
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return AllShapes.DESK_BELL.get(pState.get(FACING));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(POWERED, WATERLOGGED));
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return fluidState(pState);
    }

    @Override
    protected ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, BlockHitResult hitResult) {
        playSound(player, level, pos);
        if (level.isClient())
            return ActionResult.SUCCESS;
        level.setBlockState(pos, state.with(POWERED, true), Block.NOTIFY_ALL);
        updateNeighbours(state, level, pos);
        withBlockEntityDo(level, pos, DeskBellBlockEntity::ding);
        return ActionResult.SUCCESS;
    }

    public void playSound(@Nullable PlayerEntity pPlayer, WorldAccess pLevel, BlockPos pPos) {
        if (pLevel instanceof World level)
            AllSoundEvents.DESK_BELL_USE.play(level, pPlayer, pPos);
    }

    @Override
    public void onStateReplaced(BlockState pState, ServerWorld pLevel, BlockPos pPos, boolean pIsMoving) {
        if (!pIsMoving)
            if (pState.get(POWERED))
                updateNeighbours(pState, pLevel, pPos);
    }

    @Override
    public int getWeakRedstonePower(BlockState pBlockState, BlockView pBlockAccess, BlockPos pPos, Direction pSide) {
        return pBlockState.get(POWERED) ? 15 : 0;
    }

    @Override
    public int getStrongRedstonePower(BlockState pBlockState, BlockView pBlockAccess, BlockPos pPos, Direction pSide) {
        return pBlockState.get(POWERED) && getConnectedDirection(pBlockState) == pSide ? 15 : 0;
    }

    @Override
    public boolean emitsRedstonePower(BlockState pState) {
        return true;
    }

    public void unPress(BlockState pState, World pLevel, BlockPos pPos) {
        pLevel.setBlockState(pPos, pState.with(POWERED, false), Block.NOTIFY_ALL);
        updateNeighbours(pState, pLevel, pPos);
    }

    protected void updateNeighbours(BlockState pState, World pLevel, BlockPos pPos) {
        pLevel.updateNeighborsAlways(pPos, this, null);
        pLevel.updateNeighborsAlways(pPos.offset(getConnectedDirection(pState).getOpposite()), this, null);
    }

    private Direction getConnectedDirection(BlockState pState) {
        return pState.get(FACING, Direction.UP);
    }

    @Override
    public Class<DeskBellBlockEntity> getBlockEntityClass() {
        return DeskBellBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DeskBellBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.DESK_BELL;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

}

package com.zurrtum.create.content.contraptions.actors.trainControls;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.contraptions.ContraptionWorld;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.NotNull;

public class ControlsBlock extends HorizontalFacingBlock implements IWrenchable, ProperWaterloggedBlock {

    public static final BooleanProperty OPEN = BooleanProperty.of("open");
    public static final BooleanProperty VIRTUAL = BooleanProperty.of("virtual");

    public static final MapCodec<ControlsBlock> CODEC = createCodec(ControlsBlock::new);

    public ControlsBlock(Settings p_54120_) {
        super(p_54120_);
        setDefaultState(getDefaultState().with(OPEN, false).with(WATERLOGGED, false).with(VIRTUAL, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> pBuilder) {
        super.appendProperties(pBuilder.add(FACING, OPEN, WATERLOGGED, VIRTUAL));
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState pState,
        WorldView pLevel,
        ScheduledTickView tickView,
        BlockPos pCurrentPos,
        Direction pDirection,
        BlockPos pNeighborPos,
        BlockState pNeighborState,
        Random random
    ) {
        updateWater(pLevel, tickView, pState, pCurrentPos);
        return pState.with(OPEN, pLevel instanceof ContraptionWorld);
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return fluidState(pState);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        BlockState state = withWater(super.getPlacementState(pContext), pContext);
        Direction horizontalDirection = pContext.getHorizontalPlayerFacing();
        PlayerEntity player = pContext.getPlayer();

        state = state.with(FACING, horizontalDirection.getOpposite());
        if (player != null && player.isSneaking())
            state = state.with(FACING, horizontalDirection);

        return state;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return AllShapes.CONTROLS.get(pState.get(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return AllShapes.CONTROLS_COLLISION.get(pState.get(FACING));
    }

    @Override
    protected @NotNull MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }
}
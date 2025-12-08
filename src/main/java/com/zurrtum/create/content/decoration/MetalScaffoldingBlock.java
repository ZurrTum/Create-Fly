package com.zurrtum.create.content.decoration;

import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.ScaffoldingControlBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ScaffoldingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class MetalScaffoldingBlock extends ScaffoldingBlock implements IWrenchable, ScaffoldingControlBlock {

    public MetalScaffoldingBlock(Settings pProperties) {
        super(pProperties);
    }

    @Override
    public void scheduledTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
    }

    @Override
    public boolean canPlaceAt(BlockState pState, WorldView pLevel, BlockPos pPos) {
        return true;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        if (pState.get(BOTTOM))
            return AllShapes.SCAFFOLD_HALF;
        return super.getCollisionShape(pState, pLevel, pPos, pContext);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        if (pState.get(BOTTOM))
            return AllShapes.SCAFFOLD_HALF;
        if (pContext.isHolding(Items.AIR) || !pContext.isHolding(pState.getBlock().asItem()))
            return AllShapes.SCAFFOLD_FULL;
        return VoxelShapes.fullCube();
    }

    @Override
    public VoxelShape getRaycastShape(BlockState pState, BlockView pLevel, BlockPos pPos) {
        return VoxelShapes.fullCube();
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState pState,
        WorldView pLevel,
        ScheduledTickView tickView,
        BlockPos pCurrentPos,
        Direction pFacing,
        BlockPos pFacingPos,
        BlockState pFacingState,
        Random random
    ) {
        super.getStateForNeighborUpdate(pState, pLevel, tickView, pCurrentPos, pFacing, pFacingPos, pFacingState, random);
        BlockState stateBelow = pLevel.getBlockState(pCurrentPos.down());
        return pFacing == Direction.DOWN ? pState.with(
            BOTTOM,
            !stateBelow.isOf(this) && !stateBelow.isSideSolidFullSquare(pLevel, pCurrentPos.down(), Direction.UP)
        ) : pState;
    }

    //TODO
    //    @Override
    //    public boolean supportsExternalFaceHiding(BlockState state) {
    //        return true;
    //    }

    //TODO
    //    @Override
    //    public boolean hidesNeighborFace(BlockView level, BlockPos pos, BlockState state, BlockState neighborState, Direction dir) {
    //        if (!(neighborState.getBlock() instanceof MetalScaffoldingBlock))
    //            return false;
    //        if (!neighborState.get(BOTTOM) && state.get(BOTTOM))
    //            return false;
    //        return dir.getAxis() != Axis.Y;
    //    }

}

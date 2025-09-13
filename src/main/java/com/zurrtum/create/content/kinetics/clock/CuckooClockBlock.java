package com.zurrtum.create.content.kinetics.clock;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;

public class CuckooClockBlock extends HorizontalKineticBlock implements IBE<CuckooClockBlockEntity> {

    private final boolean mysterious;

    public static CuckooClockBlock regular(Settings properties) {
        return new CuckooClockBlock(false, properties);
    }

    public static CuckooClockBlock mysterious(Settings properties) {
        return new CuckooClockBlock(true, properties);
    }

    protected CuckooClockBlock(boolean mysterious, Settings properties) {
        super(properties);
        this.mysterious = mysterious;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState p_220053_1_, BlockView p_220053_2_, BlockPos p_220053_3_, ShapeContext p_220053_4_) {
        return AllShapes.CUCKOO_CLOCK;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Direction preferred = getPreferredHorizontalFacing(context);
        if (preferred != null)
            return getDefaultState().with(HORIZONTAL_FACING, preferred.getOpposite());
        return getDefaultState().with(HORIZONTAL_FACING, context.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return face == state.get(HORIZONTAL_FACING).getOpposite();
    }

    public static boolean containsSurprise(BlockState state) {
        Block block = state.getBlock();
        return block instanceof CuckooClockBlock && ((CuckooClockBlock) block).mysterious;
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.get(HORIZONTAL_FACING).getAxis();
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    @Override
    public Class<CuckooClockBlockEntity> getBlockEntityClass() {
        return CuckooClockBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CuckooClockBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.CUCKOO_CLOCK;
    }

}

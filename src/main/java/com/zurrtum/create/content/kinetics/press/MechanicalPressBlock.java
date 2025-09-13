package com.zurrtum.create.content.kinetics.press;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock;
import com.zurrtum.create.content.processing.basin.BasinBlock;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;

public class MechanicalPressBlock extends HorizontalKineticBlock implements IBE<MechanicalPressBlockEntity> {

    public MechanicalPressBlock(Settings properties) {
        super(properties);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        if (context instanceof EntityShapeContext entityShapeContext && entityShapeContext.getEntity() instanceof PlayerEntity)
            return AllShapes.CASING_14PX.get(Direction.DOWN);

        return AllShapes.MECHANICAL_PROCESSOR_SHAPE;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView worldIn, BlockPos pos) {
        return !BasinBlock.isBasin(worldIn, pos.down());
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Direction prefferedSide = getPreferredHorizontalFacing(context);
        if (prefferedSide != null)
            return getDefaultState().with(HORIZONTAL_FACING, prefferedSide);
        return super.getPlacementState(context);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.get(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.get(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public Class<MechanicalPressBlockEntity> getBlockEntityClass() {
        return MechanicalPressBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MechanicalPressBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.MECHANICAL_PRESS;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

}

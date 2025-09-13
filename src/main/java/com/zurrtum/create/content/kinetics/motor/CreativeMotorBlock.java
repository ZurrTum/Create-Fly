package com.zurrtum.create.content.kinetics.motor;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
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

public class CreativeMotorBlock extends DirectionalKineticBlock implements IBE<CreativeMotorBlockEntity> {

    public CreativeMotorBlock(Settings properties) {
        super(properties);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return AllShapes.MOTOR_BLOCK.get(state.get(FACING));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Direction preferred = getPreferredFacing(context);
        if ((context.getPlayer() != null && context.getPlayer().isSneaking()) || preferred == null)
            return super.getPlacementState(context);
        return getDefaultState().with(FACING, preferred);
    }

    // IRotate:

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return face == state.get(FACING);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.get(FACING).getAxis();
    }

    @Override
    public boolean hideStressImpact() {
        return true;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    @Override
    public Class<CreativeMotorBlockEntity> getBlockEntityClass() {
        return CreativeMotorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CreativeMotorBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.MOTOR;
    }

}

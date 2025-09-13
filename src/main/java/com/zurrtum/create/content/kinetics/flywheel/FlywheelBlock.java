package com.zurrtum.create.content.kinetics.flywheel;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;

public class FlywheelBlock extends RotatedPillarKineticBlock implements IBE<FlywheelBlockEntity> {

    public FlywheelBlock(Settings properties) {
        super(properties);
    }

    @Override
    public Class<FlywheelBlockEntity> getBlockEntityClass() {
        return FlywheelBlockEntity.class;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return AllShapes.LARGE_GEAR.get(pState.get(AXIS));
    }

    @Override
    public BlockEntityType<? extends FlywheelBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.FLYWHEEL;
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == getRotationAxis(state);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.get(AXIS);
    }

    @Override
    public float getParticleTargetRadius() {
        return 2f;
    }

    @Override
    public float getParticleInitialRadius() {
        return 1.75f;
    }

}

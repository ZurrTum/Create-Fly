package com.zurrtum.create.content.fluids.hosePulley;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.fluids.pipes.FluidPipeBlock;
import com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidInventoryProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class HosePulleyBlock extends HorizontalKineticBlock implements IBE<HosePulleyBlockEntity>, FluidInventoryProvider<HosePulleyBlockEntity> {

    public HosePulleyBlock(Properties properties) {
        super(properties);
    }

    @Override
    public FluidInventory getFluidInventory(
        LevelAccessor world,
        BlockPos pos,
        BlockState state,
        HosePulleyBlockEntity blockEntity,
        Direction context
    ) {
        return blockEntity.handler;
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getClockWise().getAxis();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferredHorizontalFacing = getPreferredHorizontalFacing(context);
        return defaultBlockState().setValue(
            HORIZONTAL_FACING,
            preferredHorizontalFacing != null ? preferredHorizontalFacing.getCounterClockWise() : context.getHorizontalDirection().getOpposite()
        );
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return state.getValue(HORIZONTAL_FACING).getClockWise() == face;
    }

    public static boolean hasPipeTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return state.getValue(HORIZONTAL_FACING).getCounterClockWise() == face;
    }

    @Override
    public Direction getPreferredHorizontalFacing(BlockPlaceContext context) {
        Direction fromParent = super.getPreferredHorizontalFacing(context);
        if (fromParent != null)
            return fromParent;

        Direction prefferedSide = null;
        for (Direction facing : Iterate.horizontalDirections) {
            BlockPos pos = context.getClickedPos().relative(facing);
            BlockState blockState = context.getLevel().getBlockState(pos);
            if (FluidPipeBlock.canConnectTo(context.getLevel(), pos, blockState, facing))
                if (prefferedSide != null && prefferedSide.getAxis() != facing.getAxis()) {
                    prefferedSide = null;
                    break;
                } else
                    prefferedSide = facing;
        }
        return prefferedSide == null ? null : prefferedSide.getOpposite();
    }

    @Override
    public Class<HosePulleyBlockEntity> getBlockEntityClass() {
        return HosePulleyBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends HosePulleyBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.HOSE_PULLEY;
    }

}

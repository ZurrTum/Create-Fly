package com.zurrtum.create.content.fluids.hosePulley;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.fluids.pipes.FluidPipeBlock;
import com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidInventoryProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class HosePulleyBlock extends HorizontalKineticBlock implements IBE<HosePulleyBlockEntity>, FluidInventoryProvider<HosePulleyBlockEntity> {

    public HosePulleyBlock(Settings properties) {
        super(properties);
    }

    @Override
    public FluidInventory getFluidInventory(WorldAccess world, BlockPos pos, BlockState state, HosePulleyBlockEntity blockEntity, Direction context) {
        return blockEntity.handler;
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.get(HORIZONTAL_FACING).rotateYClockwise().getAxis();
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Direction preferredHorizontalFacing = getPreferredHorizontalFacing(context);
        return getDefaultState().with(
            HORIZONTAL_FACING,
            preferredHorizontalFacing != null ? preferredHorizontalFacing.rotateYCounterclockwise() : context.getHorizontalPlayerFacing()
                .getOpposite()
        );
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return state.get(HORIZONTAL_FACING).rotateYClockwise() == face;
    }

    public static boolean hasPipeTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return state.get(HORIZONTAL_FACING).rotateYCounterclockwise() == face;
    }

    @Override
    public Direction getPreferredHorizontalFacing(ItemPlacementContext context) {
        Direction fromParent = super.getPreferredHorizontalFacing(context);
        if (fromParent != null)
            return fromParent;

        Direction prefferedSide = null;
        for (Direction facing : Iterate.horizontalDirections) {
            BlockPos pos = context.getBlockPos().offset(facing);
            BlockState blockState = context.getWorld().getBlockState(pos);
            if (FluidPipeBlock.canConnectTo(context.getWorld(), pos, blockState, facing))
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

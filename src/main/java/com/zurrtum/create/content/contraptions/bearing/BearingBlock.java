package com.zurrtum.create.content.contraptions.bearing;

import com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.WorldView;

public abstract class BearingBlock extends DirectionalKineticBlock {

    public BearingBlock(Settings properties) {
        super(properties);
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return face == state.get(FACING).getOpposite();
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.get(FACING).getAxis();
    }

    @Override
    public boolean showCapacityWithAnnotation() {
        return true;
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        ActionResult resultType = super.onWrenched(state, context);
        if (!context.getWorld().isClient && resultType.isAccepted()) {
            BlockEntity be = context.getWorld().getBlockEntity(context.getBlockPos());
            if (be instanceof MechanicalBearingBlockEntity) {
                ((MechanicalBearingBlockEntity) be).disassemble();
            }
        }
        return resultType;
    }
}

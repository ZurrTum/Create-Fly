package com.zurrtum.create.content.logistics.funnel;

import com.zurrtum.create.AllBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class BrassFunnelBlock extends FunnelBlock {

    public BrassFunnelBlock(Settings p_i48415_1_) {
        super(p_i48415_1_);
    }

    @Override
    public BlockState getEquivalentBeltFunnel(BlockView world, BlockPos pos, BlockState state) {
        Direction facing = getFacing(state);
        return AllBlocks.BRASS_BELT_FUNNEL.getDefaultState().with(BeltFunnelBlock.HORIZONTAL_FACING, facing).with(POWERED, state.get(POWERED));
    }

}

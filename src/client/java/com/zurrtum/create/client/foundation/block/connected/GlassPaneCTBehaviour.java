package com.zurrtum.create.client.foundation.block.connected;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.BlockRenderView;

public class GlassPaneCTBehaviour extends SimpleCTBehaviour {

    public GlassPaneCTBehaviour(CTSpriteShiftEntry shift) {
        super(shift);
    }

    @Override
    public boolean buildContextForOccludedDirections() {
        return true;
    }

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockRenderView reader, BlockPos pos, BlockPos otherPos, Direction face) {
        return state.getBlock() == other.getBlock();
    }

    @Override
    protected boolean reverseUVsHorizontally(BlockState state, Direction face) {
        if (face.getDirection() == AxisDirection.NEGATIVE)
            return true;
        return super.reverseUVsHorizontally(state, face);
    }
}

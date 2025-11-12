package com.zurrtum.create.mixin;

import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PistonHeadBlock.class)
public class PistonHeadBlockMixin implements NeighborUpdateListeningBlock {
    @Override
    public void neighborUpdate(BlockState sourceState, Level world, BlockPos pos, Block sourceBlock, BlockPos fromPos, boolean isMoving) {
        if (sourceState.canSurvive(world, pos)) {
            BlockPos neighborPos = pos.relative(sourceState.getValue(PistonHeadBlock.FACING).getOpposite());
            BlockState state = world.getBlockState(neighborPos);
            if (state.getBlock() instanceof NeighborUpdateListeningBlock block) {
                block.neighborUpdate(state, world, neighborPos, sourceBlock, fromPos, false);
            }
        }
    }
}

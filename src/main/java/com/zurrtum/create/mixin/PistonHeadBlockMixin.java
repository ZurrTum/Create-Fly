package com.zurrtum.create.mixin;

import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PistonHeadBlock.class)
public class PistonHeadBlockMixin implements NeighborUpdateListeningBlock {
    @Override
    public void neighborUpdate(BlockState sourceState, World world, BlockPos pos, Block sourceBlock, BlockPos fromPos, boolean isMoving) {
        if (sourceState.canPlaceAt(world, pos)) {
            BlockPos neighborPos = pos.offset(sourceState.get(PistonHeadBlock.FACING).getOpposite());
            BlockState state = world.getBlockState(neighborPos);
            if (state.getBlock() instanceof NeighborUpdateListeningBlock block) {
                block.neighborUpdate(state, world, neighborPos, sourceBlock, fromPos, false);
            }
        }
    }
}

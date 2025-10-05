package com.zurrtum.create.content.decoration;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ScaffoldingItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MetalScaffoldingBlockItem extends ScaffoldingItem {

    public MetalScaffoldingBlockItem(Block pBlock, Settings pProperties) {
        super(pBlock, pProperties);
    }

    @Nullable
    @Override
    public ItemPlacementContext getPlacementContext(ItemPlacementContext pContext) { // TODO replace with placement helper
        BlockPos blockpos = pContext.getBlockPos();
        World level = pContext.getWorld();
        BlockState blockstate = level.getBlockState(blockpos);
        Block block = this.getBlock();
        if (!blockstate.isOf(block))
            return pContext;

        Direction direction;
        if (pContext.shouldCancelInteraction()) {
            direction = pContext.hitsInsideBlock() ? pContext.getSide().getOpposite() : pContext.getSide();
        } else {
            direction = pContext.getSide() == Direction.UP ? pContext.getHorizontalPlayerFacing() : Direction.UP;
        }

        int i = 0;
        BlockPos.Mutable blockpos$mutableblockpos = blockpos.mutableCopy().move(direction);

        while (i < 7) {
            if (!level.isClient() && !level.isInBuildLimit(blockpos$mutableblockpos)) {
                PlayerEntity player = pContext.getPlayer();
                int j = level.getTopYInclusive();
                if (player instanceof ServerPlayerEntity sp && blockpos$mutableblockpos.getY() > j)
                    sp.sendMessageToClient(Text.translatable("build.tooHigh", j).formatted(Formatting.RED), true);
                break;
            }

            blockstate = level.getBlockState(blockpos$mutableblockpos);
            if (!blockstate.isOf(this.getBlock())) {
                if (blockstate.canReplace(pContext)) {
                    return ItemPlacementContext.offset(pContext, blockpos$mutableblockpos, direction);
                }
                break;
            }

            blockpos$mutableblockpos.move(direction);
            if (direction.getAxis().isHorizontal()) {
                ++i;
            }
        }

        return null;
    }

}
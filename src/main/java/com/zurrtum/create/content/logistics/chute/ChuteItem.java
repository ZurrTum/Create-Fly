package com.zurrtum.create.content.logistics.chute;

import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ChuteItem extends BlockItem {

    public ChuteItem(Block p_i48527_1_, Settings p_i48527_2_) {
        super(p_i48527_1_, p_i48527_2_);
    }

    @Override
    public ActionResult place(ItemPlacementContext context) {
        Direction face = context.getSide();
        BlockPos placedOnPos = context.getBlockPos().offset(face.getOpposite());
        World world = context.getWorld();
        BlockState placedOnState = world.getBlockState(placedOnPos);

        if (!AbstractChuteBlock.isChute(placedOnState) || context.shouldCancelInteraction())
            return super.place(context);
        if (face.getAxis().isVertical())
            return super.place(context);

        BlockPos correctPos = context.getBlockPos().up();

        BlockState blockState = world.getBlockState(correctPos);
        if (blockState.isReplaceable())
            context = ItemPlacementContext.offset(context, correctPos, face);
        else {
            if (!(blockState.getBlock() instanceof ChuteBlock block) || world.isClient())
                return ActionResult.FAIL;
            if (block.getFacing(blockState) == Direction.DOWN) {
                world.setBlockState(
                    correctPos, ProperWaterloggedBlock.withWater(
                        world,
                        block.updateChuteState(blockState.with(ChuteBlock.FACING, face), world.getBlockState(correctPos.up()), world, correctPos),
                        correctPos
                    )
                );
                return ActionResult.SUCCESS;
            }
            return ActionResult.FAIL;
        }

        return super.place(context);
    }

}

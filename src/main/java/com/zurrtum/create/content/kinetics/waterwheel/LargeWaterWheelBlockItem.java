package com.zurrtum.create.content.kinetics.waterwheel;

import com.zurrtum.create.AllClientHandle;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;

public class LargeWaterWheelBlockItem extends BlockItem {

    public LargeWaterWheelBlockItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext ctx) {
        InteractionResult result = super.place(ctx);
        if (result != InteractionResult.FAIL)
            return result;
        Direction clickedFace = ctx.getClickedFace();
        Direction.Axis axis = ((LargeWaterWheelBlock) getBlock()).getAxisForPlacement(ctx);
        if (clickedFace.getAxis() != axis)
            result = super.place(BlockPlaceContext.at(ctx, ctx.getClickedPos().relative(clickedFace), clickedFace));
        if (result == InteractionResult.FAIL && ctx.getLevel().isClientSide())
            AllClientHandle.INSTANCE.showWaterBounds(axis, ctx);
        return result;
    }
}

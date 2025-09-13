package com.zurrtum.create.content.kinetics.waterwheel;

import com.zurrtum.create.AllClientHandle;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;

public class LargeWaterWheelBlockItem extends BlockItem {

    public LargeWaterWheelBlockItem(Block pBlock, Settings pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public ActionResult place(ItemPlacementContext ctx) {
        ActionResult result = super.place(ctx);
        if (result != ActionResult.FAIL)
            return result;
        Direction clickedFace = ctx.getSide();
        Direction.Axis axis = ((LargeWaterWheelBlock) getBlock()).getAxisForPlacement(ctx);
        if (clickedFace.getAxis() != axis)
            result = super.place(ItemPlacementContext.offset(ctx, ctx.getBlockPos().offset(clickedFace), clickedFace));
        if (result == ActionResult.FAIL && ctx.getWorld().isClient())
            AllClientHandle.INSTANCE.showWaterBounds(axis, ctx);
        return result;
    }
}

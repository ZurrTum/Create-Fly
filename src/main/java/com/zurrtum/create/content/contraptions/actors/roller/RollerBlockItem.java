package com.zurrtum.create.content.contraptions.actors.roller;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class RollerBlockItem extends BlockItem {
    public RollerBlockItem(Block pBlock, Settings pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public ActionResult place(ItemPlacementContext ctx) {
        BlockPos clickedPos = ctx.getBlockPos();
        World level = ctx.getWorld();
        BlockState blockStateBelow = level.getBlockState(clickedPos.down());
        if (!Block.isFaceFullSquare(blockStateBelow.getCollisionShape(level, clickedPos.down()), Direction.UP))
            return super.place(ctx);
        Direction clickedFace = ctx.getSide();
        return super.place(ItemPlacementContext.offset(ctx, clickedPos.offset(Direction.UP), clickedFace));
    }
}

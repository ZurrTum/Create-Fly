package com.zurrtum.create.content.redstone.contact;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.contraptions.elevator.ElevatorColumn;
import com.zurrtum.create.content.contraptions.elevator.ElevatorColumn.ColumnCoords;
import com.zurrtum.create.foundation.utility.BlockHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;

public class RedstoneContactItem extends BlockItem {

    public RedstoneContactItem(Block pBlock, Settings pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    protected BlockState getPlacementState(ItemPlacementContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        BlockState state = super.getPlacementState(ctx);

        if (state == null)
            return state;
        if (!(state.getBlock() instanceof RedstoneContactBlock))
            return state;
        Direction facing = state.get(RedstoneContactBlock.FACING);
        if (facing.getAxis() == Axis.Y)
            return state;

        if (ElevatorColumn.get(world, new ColumnCoords(pos.getX(), pos.getZ(), facing)) == null)
            return state;

        return BlockHelper.copyProperties(state, AllBlocks.ELEVATOR_CONTACT.getDefaultState());
    }

}

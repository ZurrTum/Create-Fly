package com.zurrtum.create.content.redstone.contact;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.contraptions.elevator.ElevatorColumn;
import com.zurrtum.create.content.contraptions.elevator.ElevatorColumn.ColumnCoords;
import com.zurrtum.create.foundation.utility.BlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class RedstoneContactItem extends BlockItem {

    public RedstoneContactItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    @Nullable
    protected BlockState getPlacementState(BlockPlaceContext ctx) {
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockState state = super.getPlacementState(ctx);

        if (state == null)
            return state;
        if (!(state.getBlock() instanceof RedstoneContactBlock))
            return state;
        Direction facing = state.getValue(RedstoneContactBlock.FACING);
        if (facing.getAxis() == Axis.Y)
            return state;

        if (ElevatorColumn.get(world, new ColumnCoords(pos.getX(), pos.getZ(), facing)) == null)
            return state;

        return BlockHelper.copyProperties(state, AllBlocks.ELEVATOR_CONTACT.defaultBlockState());
    }

}

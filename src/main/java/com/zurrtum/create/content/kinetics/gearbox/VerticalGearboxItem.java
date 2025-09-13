package com.zurrtum.create.content.kinetics.gearbox;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.kinetics.base.IRotate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;

import java.util.Map;

public class VerticalGearboxItem extends BlockItem {

    public VerticalGearboxItem(Settings settings) {
        super(AllBlocks.GEARBOX, settings);
    }

    @Override
    public void appendBlocks(Map<Block, Item> p_195946_1_, Item p_195946_2_) {
    }

    @Override
    protected boolean postPlacement(BlockPos pos, World world, PlayerEntity player, ItemStack stack, BlockState state) {
        Axis prefferedAxis = null;
        for (Direction side : Iterate.horizontalDirections) {
            BlockState blockState = world.getBlockState(pos.offset(side));
            if (blockState.getBlock() instanceof IRotate) {
                if (((IRotate) blockState.getBlock()).hasShaftTowards(world, pos.offset(side), blockState, side.getOpposite()))
                    if (prefferedAxis != null && prefferedAxis != side.getAxis()) {
                        prefferedAxis = null;
                        break;
                    } else {
                        prefferedAxis = side.getAxis();
                    }
            }
        }

        Axis axis = prefferedAxis == null ? player.getHorizontalFacing().rotateYClockwise().getAxis() : prefferedAxis == Axis.X ? Axis.Z : Axis.X;
        world.setBlockState(pos, state.with(Properties.AXIS, axis));
        return super.postPlacement(pos, world, player, stack, state);
    }

}

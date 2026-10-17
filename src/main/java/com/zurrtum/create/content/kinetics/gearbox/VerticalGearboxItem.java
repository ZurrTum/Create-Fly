package com.zurrtum.create.content.kinetics.gearbox;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.kinetics.base.IRotate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public class VerticalGearboxItem extends BlockItem {

    public VerticalGearboxItem(Properties settings) {
        super(AllBlocks.GEARBOX, settings);
    }

    @Override
    public void registerBlocks(Map<Block, Item> p_195946_1_, Item p_195946_2_) {
    }

    @Override
    protected @Nullable BlockState getPlacementState(BlockPlaceContext context) {
        BlockState state = super.getPlacementState(context);
        if (state != null) {
            BlockPos pos = context.getClickedPos();
            Level level = context.getLevel();
            Axis prefferedAxis = null;
            for (Direction side : Iterate.horizontalDirections) {
                BlockPos neighbor = pos.relative(side);
                BlockState blockState = level.getBlockState(neighbor);
                if (blockState.getBlock() instanceof IRotate rotate) {
                    if (rotate.hasShaftTowards(level, neighbor, blockState, side.getOpposite())) {
                        if (prefferedAxis != null && prefferedAxis != side.getAxis()) {
                            prefferedAxis = null;
                            break;
                        }
                        prefferedAxis = side.getAxis();
                    }
                }
            }
            if (prefferedAxis != null) {
                return state.setValue(BlockStateProperties.AXIS, prefferedAxis == Axis.X ? Axis.Z : Axis.X);
            }
            Player player = context.getPlayer();
            if (player != null) {
                return state.setValue(BlockStateProperties.AXIS, player.getDirection().getClockWise().getAxis());
            }
        }
        return state;
    }
}

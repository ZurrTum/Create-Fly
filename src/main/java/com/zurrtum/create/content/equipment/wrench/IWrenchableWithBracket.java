package com.zurrtum.create.content.equipment.wrench;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.fluids.FluidPropagator;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Optional;

public interface IWrenchableWithBracket extends IWrenchable {

    public Optional<ItemStack> removeBracket(BlockView world, BlockPos pos, boolean inOnReplacedContext);

    @Override
    default ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        if (tryRemoveBracket(context))
            return ActionResult.SUCCESS;
        return IWrenchable.super.onWrenched(state, context);
    }

    default boolean tryRemoveBracket(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        Optional<ItemStack> bracket = removeBracket(world, pos, false);
        BlockState blockState = world.getBlockState(pos);
        if (bracket.isPresent()) {
            PlayerEntity player = context.getPlayer();
            if (!world.isClient && !player.isCreative())
                player.getInventory().offerOrDrop(bracket.get());
            if (!world.isClient && blockState.getBlock() == AllBlocks.FLUID_PIPE) {
                Axis preferred = FluidPropagator.getStraightPipeAxis(blockState);
                Direction preferredDirection = preferred == null ? Direction.UP : Direction.get(AxisDirection.POSITIVE, preferred);
                BlockState updated = AllBlocks.FLUID_PIPE.updateBlockState(blockState, preferredDirection, null, world, pos);
                if (updated != blockState)
                    world.setBlockState(pos, updated);
            }
            return true;
        }
        return false;
    }

}

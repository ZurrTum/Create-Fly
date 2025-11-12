package com.zurrtum.create.content.contraptions.glue;

import com.zurrtum.create.content.contraptions.chassis.AbstractChassisBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class SuperGlueItem extends Item {
    public SuperGlueItem(Properties settings) {
        super(settings);
    }

    public static InteractionResult glueItemAlwaysPlacesWhenUsed(Level world, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockState blockState = world.getBlockState(hit.getBlockPos());
        if (blockState.getBlock() instanceof AbstractChassisBlock cb) {
            if (cb.getGlueableSide(blockState, hit.getDirection()) != null) {
                return null;
            }
        }

        if (player.getItemInHand(hand).getItem() instanceof SuperGlueItem) {
            return InteractionResult.SUCCESS;
        }
        return null;
    }

    @Override
    public boolean canDestroyBlock(ItemStack stack, BlockState state, Level world, BlockPos pos, LivingEntity user) {
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return super.useOn(context);
    }
}

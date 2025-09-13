package com.zurrtum.create.content.contraptions.glue;

import com.zurrtum.create.content.contraptions.chassis.AbstractChassisBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SuperGlueItem extends Item {
    public SuperGlueItem(Settings settings) {
        super(settings);
    }

    public static ActionResult glueItemAlwaysPlacesWhenUsed(World world, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockState blockState = world.getBlockState(hit.getBlockPos());
        if (blockState.getBlock() instanceof AbstractChassisBlock cb) {
            if (cb.getGlueableSide(blockState, hit.getSide()) != null) {
                return null;
            }
        }

        if (player.getStackInHand(hand).getItem() instanceof SuperGlueItem) {
            return ActionResult.SUCCESS;
        }
        return null;
    }

    @Override
    public boolean canMine(ItemStack stack, BlockState state, World world, BlockPos pos, LivingEntity user) {
        return false;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return super.useOnBlock(context);
    }
}

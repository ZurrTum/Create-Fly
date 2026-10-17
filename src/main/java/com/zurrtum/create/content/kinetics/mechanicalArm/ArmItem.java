package com.zurrtum.create.content.kinetics.mechanicalArm;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ArmItem extends BlockItem {

    public ArmItem(Block p_i48527_1_, Properties p_i48527_2_) {
        super(p_i48527_1_, p_i48527_2_);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        if (ArmInteractionPoint.isInteractable(world, pos, world.getBlockState(pos))) {
            return InteractionResult.SUCCESS;
        }
        return super.useOn(ctx);
    }

    @Override
    public boolean canDestroyBlock(
        ItemStack stack,
        BlockState state,
        Level world,
        BlockPos pos,
        LivingEntity p_195938_4_
    ) {
        return !ArmInteractionPoint.isInteractable(world, pos, state);
    }

}

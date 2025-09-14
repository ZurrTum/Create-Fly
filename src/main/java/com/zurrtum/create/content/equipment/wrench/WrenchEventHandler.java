package com.zurrtum.create.content.equipment.wrench;

import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WrenchEventHandler {
    public static ActionResult useOwnWrenchLogicForCreateBlocks(
        World world,
        PlayerEntity player,
        ItemStack itemStack,
        Hand hand,
        BlockHitResult hitVec,
        BlockPos pos
    ) {
        if (world == null || player == null || !player.canModifyBlocks() || itemStack.isEmpty() || itemStack.isOf(AllItems.WRENCH))
            return null;
        if (!itemStack.isIn(AllItemTags.TOOLS_WRENCH))
            return null;

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (!(block instanceof IWrenchable actor))
            return null;

        ItemUsageContext context = new ItemUsageContext(player, hand, hitVec);
        return player.isSneaking() ? actor.onSneakWrenched(state, context) : actor.onWrenched(state, context);
    }
}

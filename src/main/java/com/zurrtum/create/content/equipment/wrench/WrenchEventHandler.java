package com.zurrtum.create.content.equipment.wrench;

import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class WrenchEventHandler {
    public static InteractionResult useOwnWrenchLogicForCreateBlocks(
        Level world,
        Player player,
        ItemStack itemStack,
        InteractionHand hand,
        BlockHitResult hitVec,
        BlockPos pos
    ) {
        if (world == null || player == null || !player.mayBuild() || itemStack.isEmpty() || itemStack.is(AllItems.WRENCH))
            return null;
        if (!itemStack.is(AllItemTags.TOOLS_WRENCH))
            return null;

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (!(block instanceof IWrenchable actor))
            return null;

        UseOnContext context = new UseOnContext(player, hand, hitVec);
        return player.isShiftKeyDown() ? actor.onSneakWrenched(state, context) : actor.onWrenched(state, context);
    }
}

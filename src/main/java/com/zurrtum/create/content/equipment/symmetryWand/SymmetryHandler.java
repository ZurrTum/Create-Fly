package com.zurrtum.create.content.equipment.symmetryWand;

import com.zurrtum.create.AllItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SymmetryHandler {
    public static void onBlockPlaced(ServerLevel world, Player player, BlockPos pos, BlockPlaceContext context) {
        Inventory inv = player.getInventory();
        Direction side = context.getClickedFace();
        InteractionHand hand = context.getHand();
        Vec3 hitPos = context.getClickLocation();
        boolean canReplaceExisting = context.replacingClickedOnBlock();
        BlockState block = null;
        for (int i = 0, size = Inventory.getSelectionSize(); i < size; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(AllItems.WAND_OF_SYMMETRY)) {
                if (block == null) {
                    block = world.getBlockState(pos);
                }
                SymmetryWandItem.apply(world, stack, player, pos, block, hitPos, canReplaceExisting, side, hand);
            }
        }
    }

    public static void onBlockDestroyed(ServerPlayer player, BlockPos pos, BlockState state) {
        Inventory inv = player.getInventory();
        for (int i = 0, size = Inventory.getSelectionSize(); i < size; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(AllItems.WAND_OF_SYMMETRY)) {
                SymmetryWandItem.remove(player.level(), stack, player, pos, state);
            }
        }
    }
}

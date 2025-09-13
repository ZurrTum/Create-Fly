package com.zurrtum.create.content.equipment.symmetryWand;

import com.zurrtum.create.AllItems;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class SymmetryHandler {
    public static void onBlockPlaced(ServerWorld world, PlayerEntity player, BlockPos pos, ItemPlacementContext context) {
        PlayerInventory inv = player.getInventory();
        Direction side = context.getSide();
        Hand hand = context.getHand();
        Vec3d hitPos = context.getHitPos();
        boolean canReplaceExisting = context.canReplaceExisting();
        BlockState block = null;
        for (int i = 0, size = PlayerInventory.getHotbarSize(); i < size; i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isOf(AllItems.WAND_OF_SYMMETRY)) {
                if (block == null) {
                    block = world.getBlockState(pos);
                }
                SymmetryWandItem.apply(world, stack, player, pos, block, hitPos, canReplaceExisting, side, hand);
            }
        }
    }

    public static void onBlockDestroyed(ServerPlayerEntity player, BlockPos pos, BlockState state) {
        PlayerInventory inv = player.getInventory();
        for (int i = 0, size = PlayerInventory.getHotbarSize(); i < size; i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isOf(AllItems.WAND_OF_SYMMETRY)) {
                SymmetryWandItem.remove(player.getWorld(), stack, player, pos, state);
            }
        }
    }
}

package com.zurrtum.create.content.decoration.encasing;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Implement this interface to indicate that this block is encased.
 */
public interface EncasedBlock {
    Block getCasing();

    /**
     * Handles how encasing should be done if {@link EncasableBlock#tryEncase(BlockState, World, BlockPos, ItemStack, PlayerEntity, Hand, BlockHitResult)} is successful.
     */
    default void handleEncasing(BlockState state, World level, BlockPos pos, ItemStack heldItem, PlayerEntity player, Hand hand, BlockHitResult ray) {
    }
}

package com.zurrtum.create.client.infrastructure.click;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public interface ClientRightClickHandle {
    InteractionResult onRightClickBlock(Level world, LocalPlayer player, ItemStack stack, InteractionHand hand, BlockHitResult hit, BlockPos pos);
}

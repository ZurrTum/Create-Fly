package com.zurrtum.create.infrastructure.click;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public interface ServerRightClickHandle {
    InteractionResult onRightClickBlock(Level world, ServerPlayer player, ItemStack stack, InteractionHand hand, BlockHitResult hit, BlockPos pos);
}

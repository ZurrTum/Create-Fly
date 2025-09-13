package com.zurrtum.create.infrastructure.click;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ServerRightClickHandle {
    ActionResult onRightClickBlock(World world, ServerPlayerEntity player, ItemStack stack, Hand hand, BlockHitResult hit, BlockPos pos);
}

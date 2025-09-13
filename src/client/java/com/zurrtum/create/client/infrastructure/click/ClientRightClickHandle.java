package com.zurrtum.create.client.infrastructure.click;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ClientRightClickHandle {
    ActionResult onRightClickBlock(World world, ClientPlayerEntity player, ItemStack stack, Hand hand, BlockHitResult hit, BlockPos pos);
}

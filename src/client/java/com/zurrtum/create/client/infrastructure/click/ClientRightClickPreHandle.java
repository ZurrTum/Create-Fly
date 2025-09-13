package com.zurrtum.create.client.infrastructure.click;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

public interface ClientRightClickPreHandle {
    ActionResult onRightClickBlock(World world, ClientPlayerEntity player, Hand hand, BlockHitResult ray);
}

package com.zurrtum.create.client.infrastructure.click;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public interface ClientRightClickPreHandle {
    InteractionResult onRightClickBlock(Level world, LocalPlayer player, InteractionHand hand, BlockHitResult ray);
}

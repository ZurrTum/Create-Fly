package com.zurrtum.create.content.processing;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AssemblyOperatorUseContext extends ItemPlacementContext {
    protected AssemblyOperatorUseContext(
        World world,
        @Nullable PlayerEntity playerEntity,
        Hand hand,
        ItemStack itemStack,
        BlockHitResult blockHitResult
    ) {
        super(world, playerEntity, hand, itemStack, blockHitResult);
    }
}

package com.zurrtum.create.content.processing;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class AssemblyOperatorUseContext extends BlockPlaceContext {
    protected AssemblyOperatorUseContext(
        Level world,
        @Nullable Player playerEntity,
        InteractionHand hand,
        ItemStack itemStack,
        BlockHitResult blockHitResult
    ) {
        super(world, playerEntity, hand, itemStack, blockHitResult);
    }
}

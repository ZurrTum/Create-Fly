package com.zurrtum.create.foundation.utility;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class AbstractBlockBreakQueue {
    protected Consumer<BlockPos> makeCallbackFor(
        World world,
        float effectChance,
        ItemStack toDamage,
        @Nullable PlayerEntity playerEntity,
        BiConsumer<BlockPos, ItemStack> drop
    ) {
        return pos -> {
            BlockHelper.destroyBlockAs(world, pos, playerEntity, toDamage, effectChance, stack -> drop.accept(pos, stack));
        };
    }

    public void destroyBlocks(World world, @Nullable LivingEntity entity, BiConsumer<BlockPos, ItemStack> drop) {
        PlayerEntity playerEntity = entity instanceof PlayerEntity ? ((PlayerEntity) entity) : null;
        ItemStack toDamage = playerEntity != null && !playerEntity.isCreative() ? playerEntity.getMainHandStack() : ItemStack.EMPTY;
        destroyBlocks(world, toDamage, playerEntity, drop);
    }

    public abstract void destroyBlocks(World world, ItemStack toDamage, @Nullable PlayerEntity playerEntity, BiConsumer<BlockPos, ItemStack> drop);
}

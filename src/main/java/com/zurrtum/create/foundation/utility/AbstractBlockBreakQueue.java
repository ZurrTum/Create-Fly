package com.zurrtum.create.foundation.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class AbstractBlockBreakQueue {
    protected Consumer<BlockPos> makeCallbackFor(
        Level world,
        float effectChance,
        ItemStack toDamage,
        @Nullable Player playerEntity,
        BiConsumer<BlockPos, ItemStack> drop
    ) {
        return pos -> {
            BlockHelper.destroyBlockAs(world, pos, playerEntity, toDamage, effectChance, stack -> drop.accept(pos, stack));
        };
    }

    public void destroyBlocks(Level world, @Nullable LivingEntity entity, BiConsumer<BlockPos, ItemStack> drop) {
        Player playerEntity = entity instanceof Player ? ((Player) entity) : null;
        ItemStack toDamage = playerEntity != null && !playerEntity.isCreative() ? playerEntity.getMainHandItem() : ItemStack.EMPTY;
        destroyBlocks(world, toDamage, playerEntity, drop);
    }

    public abstract void destroyBlocks(Level world, ItemStack toDamage, @Nullable Player playerEntity, BiConsumer<BlockPos, ItemStack> drop);
}

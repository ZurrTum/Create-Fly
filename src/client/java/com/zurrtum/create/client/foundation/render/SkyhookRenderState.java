package com.zurrtum.create.client.foundation.render;

import java.util.UUID;

import net.minecraft.world.item.ItemStack;

public interface SkyhookRenderState {
    void create$setMainStack(ItemStack stack);

    ItemStack create$getMainStack();

    void create$setUuid(UUID uuid);

    UUID create$getUuid();
}

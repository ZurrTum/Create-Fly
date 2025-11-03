package com.zurrtum.create.client.foundation.render;

import net.minecraft.item.ItemStack;

import java.util.UUID;

public interface SkyhookRenderState {
    void create$setMainStack(ItemStack stack);

    ItemStack create$getMainStack();

    void create$setUuid(UUID uuid);

    UUID create$getUuid();
}

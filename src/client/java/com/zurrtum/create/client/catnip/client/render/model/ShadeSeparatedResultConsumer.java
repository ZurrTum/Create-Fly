package com.zurrtum.create.client.catnip.client.render.model;

import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.BuiltBuffer;

public interface ShadeSeparatedResultConsumer {
    void accept(BlockRenderLayer renderType, boolean shaded, BuiltBuffer data);
}

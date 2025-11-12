package com.zurrtum.create.client.foundation.block.connected;

import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour.CTContext;
import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour.ContextRequirement;
import net.minecraft.resources.ResourceLocation;

public interface CTType {
    ResourceLocation getId();

    int getSheetSize();

    ContextRequirement getContextRequirement();

    int getTextureIndex(CTContext context);
}

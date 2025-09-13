package com.zurrtum.create.client.foundation.block.connected;

import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour.CTContext;
import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour.ContextRequirement;
import net.minecraft.util.Identifier;

public interface CTType {
    Identifier getId();

    int getSheetSize();

    ContextRequirement getContextRequirement();

    int getTextureIndex(CTContext context);
}

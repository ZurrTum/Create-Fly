package com.zurrtum.create.client.flywheel.impl.compat;

import com.zurrtum.create.client.flywheel.impl.FlwImplXplat;

public enum CompatMod {
    EMBEDDIUM("embeddium"),
    IRIS("iris"),
    SODIUM("sodium");

    public final String id;
    public final boolean isLoaded;

    CompatMod(String modId) {
        id = modId;
        isLoaded = FlwImplXplat.INSTANCE.isModLoaded(modId);
    }
}
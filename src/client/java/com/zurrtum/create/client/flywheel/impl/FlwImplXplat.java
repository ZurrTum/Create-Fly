package com.zurrtum.create.client.flywheel.impl;

import net.minecraft.client.world.ClientWorld;

public interface FlwImplXplat {
    FlwImplXplat INSTANCE = new FlwImplXplatImpl();

    boolean isModLoaded(String modId);

    void dispatchReloadLevelRendererEvent(ClientWorld level);

    String getVersionStr();

    FlwConfig getConfig();
}

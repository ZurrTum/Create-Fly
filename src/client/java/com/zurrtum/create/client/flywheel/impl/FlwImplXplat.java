package com.zurrtum.create.client.flywheel.impl;

import net.minecraft.client.multiplayer.ClientLevel;

public interface FlwImplXplat {
    FlwImplXplat INSTANCE = new FlwImplXplatImpl();

    boolean isModLoaded(String modId);

    void dispatchReloadLevelRendererEvent(ClientLevel level);

    String getVersionStr();

    FlwConfig getConfig();
}

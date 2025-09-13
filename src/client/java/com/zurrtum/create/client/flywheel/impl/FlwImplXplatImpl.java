package com.zurrtum.create.client.flywheel.impl;

import com.zurrtum.create.client.content.contraptions.render.ContraptionRenderInfoManager;
import com.zurrtum.create.client.flywheel.backend.engine.uniform.Uniforms;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.world.ClientWorld;

public class FlwImplXplatImpl implements FlwImplXplat {
    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public void dispatchReloadLevelRendererEvent(ClientWorld level) {
        BackendManagerImpl.onReloadLevelRenderer(level);
        Uniforms.onReloadLevelRenderer();
        ContraptionRenderInfoManager.onReloadLevelRenderer();
    }

    @Override
    public String getVersionStr() {
        return Flywheel.version().getFriendlyString();
    }

    @Override
    public FlwConfig getConfig() {
        return FabricFlwConfig.INSTANCE;
    }
}

package com.zurrtum.create.client.flywheel.impl;

import com.zurrtum.create.client.flywheel.backend.engine.uniform.Uniforms;
import com.zurrtum.create.client.flywheel.lib.model.baked.ModelRenderHelper;
import com.zurrtum.create.client.flywheel.lib.util.RendererReloadCache;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.multiplayer.ClientLevel;

public class FlwImplXplatImpl implements FlwImplXplat {
    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public void dispatchReloadLevelRendererEvent(ClientLevel level) {
        RendererReloadCache.onReloadLevelRenderer();
        BackendManagerImpl.onReloadLevelRenderer(level);
        Uniforms.onReloadLevelRenderer();
        ModelRenderHelper.onReloadLevelRenderer();
        //TODO Fabric
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

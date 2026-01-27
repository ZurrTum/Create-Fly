package com.zurrtum.create.client.catnip.event;

import com.zurrtum.create.client.catnip.lang.LangNumberFormat;
import com.zurrtum.create.client.ponder.Ponder;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class ClientResourceReloadListener implements ResourceManagerReloadListener {
    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        LangNumberFormat.numberFormat.update();
        Ponder.invalidateRenderers();
    }
}

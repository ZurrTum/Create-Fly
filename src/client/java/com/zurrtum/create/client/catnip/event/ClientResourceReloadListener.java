package com.zurrtum.create.client.catnip.event;

import com.zurrtum.create.client.catnip.lang.LangNumberFormat;
import com.zurrtum.create.client.ponder.Ponder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;

public class ClientResourceReloadListener implements SynchronousResourceReloader {
    @Override
    public void reload(ResourceManager resourceManager) {
        LangNumberFormat.numberFormat.update();
        Ponder.invalidateRenderers();
    }
}

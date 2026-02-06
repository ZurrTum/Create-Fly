package com.zurrtum.create.client.catnip.event;

import com.zurrtum.create.client.catnip.lang.LangNumberFormat;
import com.zurrtum.create.client.ponder.Ponder;
import com.zurrtum.create.foundation.utility.CreateResourceReloader;
import net.minecraft.resource.ResourceManager;

public class ClientResourceReloadListener extends CreateResourceReloader {
    public ClientResourceReloadListener() {
        super("ponder");
    }

    @Override
    public void reload(ResourceManager resourceManager) {
        LangNumberFormat.numberFormat.update();
        Ponder.invalidateRenderers();
    }
}

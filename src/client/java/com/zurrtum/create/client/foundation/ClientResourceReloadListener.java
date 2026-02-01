package com.zurrtum.create.client.foundation;

import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.foundation.sound.SoundScapes;
import com.zurrtum.create.client.infrastructure.model.TableClothModel;
import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import com.zurrtum.create.foundation.utility.CreateResourceReloader;
import net.minecraft.server.packs.resources.ResourceManager;

public class ClientResourceReloadListener extends CreateResourceReloader {
    public ClientResourceReloadListener() {
        super("resource");
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        Create.invalidateRenderers();
        SoundScapes.invalidateAll();
        BeltHelper.uprightCache.clear();
        TableClothModel.reload();
    }

}
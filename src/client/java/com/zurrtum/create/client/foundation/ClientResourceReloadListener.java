package com.zurrtum.create.client.foundation;

import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.foundation.sound.SoundScapes;
import com.zurrtum.create.client.infrastructure.model.TableClothModel;
import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;

public class ClientResourceReloadListener implements SynchronousResourceReloader {
    @Override
    public void reload(ResourceManager resourceManager) {
        Create.invalidateRenderers();
        SoundScapes.invalidateAll();
        BeltHelper.uprightCache.clear();
        TableClothModel.reload();
    }

}
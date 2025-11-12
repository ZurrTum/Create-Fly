package com.zurrtum.create.client.ponder.api.registration;

import net.minecraft.resources.ResourceLocation;

public interface LangRegistryAccess {

    String getShared(ResourceLocation key);

    String getShared(ResourceLocation key, Object... params);

    String getTagName(ResourceLocation key);

    String getTagDescription(ResourceLocation key);

    String getSpecific(ResourceLocation sceneId, String k);

    String getSpecific(ResourceLocation sceneId, String k, Object... params);

}
package com.zurrtum.create.client.ponder.api.registration;

import net.minecraft.resources.Identifier;

public interface LangRegistryAccess {

    String getShared(Identifier key);

    String getShared(Identifier key, Object... params);

    String getTagName(Identifier key);

    String getTagDescription(Identifier key);

    String getSpecific(Identifier sceneId, String k);

    String getSpecific(Identifier sceneId, String k, Object... params);

}
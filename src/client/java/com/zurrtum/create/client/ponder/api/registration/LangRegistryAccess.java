package com.zurrtum.create.client.ponder.api.registration;

import net.minecraft.util.Identifier;

public interface LangRegistryAccess {

    String getShared(Identifier key);

    String getTagName(Identifier key);

    String getTagDescription(Identifier key);

    String getSpecific(Identifier sceneId, String k);

}
package com.zurrtum.create.client.ponder.api.registration;

import com.zurrtum.create.client.ponder.foundation.PonderScene;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface SceneRegistryAccess {

    boolean doScenesExistForId(Identifier id);

    Collection<Map.Entry<Identifier, StoryBoardEntry>> getRegisteredEntries();

    List<PonderScene> compile(Identifier id);

    List<PonderScene> compile(Collection<StoryBoardEntry> entries);

}
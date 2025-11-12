package com.zurrtum.create.client.ponder.api.registration;

import com.zurrtum.create.client.ponder.foundation.PonderScene;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public interface SceneRegistryAccess {

    boolean doScenesExistForId(ResourceLocation id);

    Collection<Map.Entry<ResourceLocation, StoryBoardEntry>> getRegisteredEntries();

    List<PonderScene> compile(ResourceLocation id);

    List<PonderScene> compile(Collection<StoryBoardEntry> entries);

}
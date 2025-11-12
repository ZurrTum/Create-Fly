package com.zurrtum.create.client.ponder.api.registration;

import com.zurrtum.create.client.ponder.foundation.PonderTag;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public interface TagRegistryAccess {

    PonderTag getRegisteredTag(ResourceLocation tagLocation);

    List<PonderTag> getListedTags();

    Set<PonderTag> getTags(ResourceLocation item);

    Set<ResourceLocation> getItems(ResourceLocation tag);

    Set<ResourceLocation> getItems(PonderTag tag);

}
package com.zurrtum.create.client.ponder.api.registration;

import com.zurrtum.create.client.ponder.foundation.PonderTag;

import java.util.List;
import java.util.Set;

import net.minecraft.resources.Identifier;

public interface TagRegistryAccess {

    PonderTag getRegisteredTag(Identifier tagLocation);

    List<PonderTag> getListedTags();

    Set<PonderTag> getTags(Identifier item);

    Set<Identifier> getItems(Identifier tag);

    Set<Identifier> getItems(PonderTag tag);

}
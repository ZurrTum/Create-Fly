package com.zurrtum.create.client.ponder.foundation.registration;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.zurrtum.create.client.ponder.Ponder;
import com.zurrtum.create.client.ponder.api.registration.TagRegistryAccess;
import com.zurrtum.create.client.ponder.foundation.PonderTag;

import java.util.*;
import java.util.stream.Collectors;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

public class PonderTagRegistry implements TagRegistryAccess {

    private final PonderLocalization localization;
    private final Multimap<Identifier, Identifier> componentTagMap;
    private final Map<Identifier, PonderTag> registeredTags;
    private final List<PonderTag> listedTags;

    private final PonderTag MISSING = new PonderTag(
        Ponder.asResource("not_registered"),
        null,
        Items.BARRIER.getDefaultInstance(),
        Items.BARRIER.getDefaultInstance()
    );

    private boolean allowRegistration = true;

    public PonderTagRegistry(PonderLocalization localization) {
        this.localization = localization;
        componentTagMap = LinkedHashMultimap.create();
        registeredTags = new HashMap<>();
        listedTags = new ArrayList<>();
    }

    public void clearRegistry() {
        componentTagMap.clear();
        listedTags.clear();
        allowRegistration = true;
    }

    //

    public void registerTag(PonderTag tag) {
        if (!allowRegistration)
            throw new IllegalStateException("Registration Phase has already ended!");

        registeredTags.put(tag.getId(), tag);
    }

    public void listTag(PonderTag tag) {
        if (!allowRegistration)
            throw new IllegalStateException("Registration Phase has already ended!");

        listedTags.add(tag);
    }

    public void addTagToComponent(Identifier tag, Identifier item) {
        if (!allowRegistration)
            throw new IllegalStateException("Registration Phase has already ended!");

        synchronized (componentTagMap) {
            componentTagMap.put(item, tag);
        }
    }

    //

    @Override
    public PonderTag getRegisteredTag(Identifier tagLocation) {
        return registeredTags.getOrDefault(tagLocation, MISSING);
    }

    @Override
    public List<PonderTag> getListedTags() {
        return listedTags;
    }

    @Override
    public Set<PonderTag> getTags(Identifier item) {
        return componentTagMap.get(item).stream().map(this::getRegisteredTag).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<Identifier> getItems(Identifier tag) {
        return componentTagMap.entries().stream().filter(e -> e.getValue().equals(tag)).map(Map.Entry::getKey).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public Set<Identifier> getItems(PonderTag tag) {
        return getItems(tag.getId());
    }

}
package com.zurrtum.create.client.ponder.foundation.registration;

import com.zurrtum.create.client.ponder.api.registration.MultiTagBuilder;
import com.zurrtum.create.client.ponder.api.registration.PonderTagRegistrationHelper;
import com.zurrtum.create.client.ponder.api.registration.TagBuilder;
import com.zurrtum.create.client.ponder.foundation.PonderTag;
import com.zurrtum.create.client.ponder.foundation.registration.GenericMultiTagBuilder.Component;
import com.zurrtum.create.client.ponder.foundation.registration.GenericMultiTagBuilder.Tag;

import java.util.List;
import java.util.function.Function;

import net.minecraft.resources.Identifier;

public class DefaultPonderTagRegistrationHelper implements PonderTagRegistrationHelper<Identifier> {

    protected String namespace;
    protected PonderTagRegistry tagRegistry;
    protected PonderLocalization localization;

    public DefaultPonderTagRegistrationHelper(String namespace, PonderTagRegistry tagRegistry, PonderLocalization localization) {
        this.namespace = namespace;
        this.tagRegistry = tagRegistry;
        this.localization = localization;
    }

    @Override
    public <T> PonderTagRegistrationHelper<T> withKeyFunction(Function<T, Identifier> keyGen) {
        return new GenericPonderTagRegistrationHelper<>(this, keyGen);
    }

    @Override
    public TagBuilder registerTag(Identifier location) {
        return new PonderTagBuilder(location, this::finishTagRegister);
    }

    @Override
    public TagBuilder registerTag(String id) {
        return new PonderTagBuilder(Identifier.fromNamespaceAndPath(namespace, id), this::finishTagRegister);
    }

    private void finishTagRegister(PonderTagBuilder builder) {
        localization.registerTag(builder.id, builder.title, builder.description);

        PonderTag tag = new PonderTag(builder.id, builder.textureIconLocation, builder.itemIcon, builder.mainItem);
        tagRegistry.registerTag(tag);

        if (builder.addToIndex)
            tagRegistry.listTag(tag);
    }

    @Override
    public void addTagToComponent(Identifier component, Identifier tag) {
        tagRegistry.addTagToComponent(tag, component);
    }

    @Override
    public MultiTagBuilder.Tag<Identifier> addToTag(Identifier tag) {
        return new GenericMultiTagBuilder<Identifier>().new Tag(this, List.of(tag));
    }

    @Override
    public MultiTagBuilder.Tag<Identifier> addToTag(Identifier... tags) {
        return new GenericMultiTagBuilder<Identifier>().new Tag(this, List.of(tags));
    }

    @Override
    public MultiTagBuilder.Component addToComponent(Identifier component) {
        return new GenericMultiTagBuilder<Identifier>().new Component(this, List.of(component));
    }

    @Override
    public MultiTagBuilder.Component addToComponent(Identifier... components) {
        return new GenericMultiTagBuilder<Identifier>().new Component(this, List.of(components));
    }
}
package com.zurrtum.create.client.ponder.foundation.registration;

import com.zurrtum.create.client.ponder.api.registration.MultiTagBuilder;
import com.zurrtum.create.client.ponder.api.registration.PonderTagRegistrationHelper;
import com.zurrtum.create.client.ponder.api.registration.TagBuilder;
import com.zurrtum.create.client.ponder.foundation.registration.GenericMultiTagBuilder.Component;
import com.zurrtum.create.client.ponder.foundation.registration.GenericMultiTagBuilder.Tag;

import java.util.List;
import java.util.function.Function;

import net.minecraft.resources.Identifier;

public class GenericPonderTagRegistrationHelper<T> implements PonderTagRegistrationHelper<T> {

    private final PonderTagRegistrationHelper<Identifier> helperDelegate;
    private final Function<T, Identifier> keyGen;

    public GenericPonderTagRegistrationHelper(PonderTagRegistrationHelper<Identifier> helperDelegate, Function<T, Identifier> keyGen) {
        this.helperDelegate = helperDelegate;
        this.keyGen = keyGen;
    }

    @Override
    public <S> PonderTagRegistrationHelper<S> withKeyFunction(Function<S, T> keyGen) {
        return new GenericPonderTagRegistrationHelper<>(helperDelegate, keyGen.andThen(this.keyGen));
    }

    @Override
    public TagBuilder registerTag(Identifier location) {
        return helperDelegate.registerTag(location);
    }

    @Override
    public TagBuilder registerTag(String id) {
        return helperDelegate.registerTag(id);
    }

    @Override
    public void addTagToComponent(T component, Identifier tag) {
        helperDelegate.addTagToComponent(keyGen.apply(component), tag);
    }

    @Override
    public MultiTagBuilder.Tag<T> addToTag(Identifier tag) {
        return new GenericMultiTagBuilder<T>().new Tag(this, List.of(tag));
    }

    @Override
    public MultiTagBuilder.Tag<T> addToTag(Identifier... tags) {
        return new GenericMultiTagBuilder<T>().new Tag(this, List.of(tags));
    }

    @Override
    public MultiTagBuilder.Component addToComponent(T component) {
        return new GenericMultiTagBuilder<T>().new Component(this, List.of(component));
    }

    @Override
    @SafeVarargs
    public final MultiTagBuilder.Component addToComponent(T... components) {
        return new GenericMultiTagBuilder<T>().new Component(this, List.of(components));
    }
}
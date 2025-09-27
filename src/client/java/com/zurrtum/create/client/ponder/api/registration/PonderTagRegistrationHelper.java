package com.zurrtum.create.client.ponder.api.registration;

import net.minecraft.util.Identifier;

import java.util.function.Function;

public interface PonderTagRegistrationHelper<T> {

    <S> PonderTagRegistrationHelper<S> withKeyFunction(Function<S, T> keyGen);

    TagBuilder registerTag(Identifier location);

    TagBuilder registerTag(String id);

    void addTagToComponent(T component, Identifier tag);

    MultiTagBuilder.Tag<T> addToTag(Identifier tag);

    MultiTagBuilder.Tag<T> addToTag(Identifier... tags);

    MultiTagBuilder.Component addToComponent(T component);

    MultiTagBuilder.Component addToComponent(T... component);

}

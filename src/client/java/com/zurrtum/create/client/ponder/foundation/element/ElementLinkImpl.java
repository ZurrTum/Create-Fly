package com.zurrtum.create.client.ponder.foundation.element;

import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.PonderElement;

import java.util.UUID;

public class ElementLinkImpl<T extends PonderElement> implements ElementLink<T> {

    private final Class<T> elementClass;
    private final UUID id;

    public ElementLinkImpl(Class<T> elementClass) {
        this(elementClass, UUID.randomUUID());
    }

    public ElementLinkImpl(Class<T> elementClass, UUID id) {
        this.elementClass = elementClass;
        this.id = id;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public T cast(PonderElement e) {
        return elementClass.cast(e);
    }

}
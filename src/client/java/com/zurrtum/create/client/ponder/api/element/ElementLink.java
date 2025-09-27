package com.zurrtum.create.client.ponder.api.element;

import java.util.UUID;

public interface ElementLink<T extends PonderElement> {
    UUID getId();

    T cast(PonderElement e);
}
package com.zurrtum.create.client.ponder.api.registration;

import net.minecraft.util.Identifier;

public interface MultiTagBuilder {

    interface Tag<T> {

        Tag<T> add(T component);

    }

    interface Component {

        Component add(Identifier tag);

    }

}
package com.zurrtum.create.client.foundation.ponder.element;

import com.zurrtum.create.client.ponder.foundation.element.TrackedElementBase;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;

public class BeltItemElement extends TrackedElementBase<TransportedItemStack> {

    public BeltItemElement(TransportedItemStack wrapped) {
        super(wrapped);
    }

}
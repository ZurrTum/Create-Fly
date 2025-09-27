package com.zurrtum.create.client.ponder.foundation.element;

import com.zurrtum.create.client.ponder.api.element.EntityElement;
import net.minecraft.entity.Entity;

public class EntityElementImpl extends TrackedElementBase<Entity> implements EntityElement {

    public EntityElementImpl(Entity wrapped) {
        super(wrapped);
    }

    @Override
    public boolean isStillValid(Entity element) {
        return element.isAlive();
    }

}
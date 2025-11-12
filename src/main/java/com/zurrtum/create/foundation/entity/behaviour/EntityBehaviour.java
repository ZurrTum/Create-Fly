package com.zurrtum.create.foundation.entity.behaviour;

import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import net.minecraft.world.entity.Entity;

public abstract class EntityBehaviour<T extends Entity> {
    public T entity;

    public EntityBehaviour(T entity) {
        this.entity = entity;
    }

    public abstract BehaviourType<?> getType();

    public void tick() {
    }

    public void destroy() {
    }
}

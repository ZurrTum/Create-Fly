package com.zurrtum.create.api.behaviour;

import com.zurrtum.create.api.registry.SimpleRegistry;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.function.Function;

public abstract class EntityBehaviour<T extends Entity> {
    public static final SimpleRegistry.Multi<EntityType<?>, Function<Entity, EntityBehaviour<?>>> REGISTRY = SimpleRegistry.Multi.create();
    public static final SimpleRegistry.Multi<EntityType<?>, Function<Entity, EntityBehaviour<?>>> CLIENT_REGISTRY = SimpleRegistry.Multi.create();

    @SuppressWarnings("unchecked")
    public static <T extends Entity> void add(EntityType<T> type, Function<T, EntityBehaviour<?>> factory) {
        REGISTRY.add(type, (Function<Entity, EntityBehaviour<?>>) factory);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> void addClient(EntityType<T> type, Function<T, EntityBehaviour<?>> factory) {
        CLIENT_REGISTRY.add(type, (Function<Entity, EntityBehaviour<?>>) factory);
    }

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

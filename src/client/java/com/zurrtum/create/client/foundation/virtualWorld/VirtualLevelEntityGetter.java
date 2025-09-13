package com.zurrtum.create.client.foundation.virtualWorld;

import net.minecraft.util.TypeFilter;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.util.math.Box;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityLookup;

import java.util.Collections;
import java.util.UUID;
import java.util.function.Consumer;

public class VirtualLevelEntityGetter<T extends EntityLike> implements EntityLookup<T> {
    @Override
    public T get(int id) {
        return null;
    }

    @Override
    public T get(UUID uuid) {
        return null;
    }

    @Override
    public Iterable<T> iterate() {
        return Collections.emptyList();
    }

    @Override
    public <U extends T> void forEach(TypeFilter<T, U> test, LazyIterationConsumer<U> consumer) {
    }

    @Override
    public void forEachIntersects(Box boundingBox, Consumer<T> consumer) {
    }

    @Override
    public <U extends T> void forEachIntersects(TypeFilter<T, U> test, Box bounds, LazyIterationConsumer<U> consumer) {
    }
}

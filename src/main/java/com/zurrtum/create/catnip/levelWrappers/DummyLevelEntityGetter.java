package com.zurrtum.create.catnip.levelWrappers;

import net.minecraft.util.TypeFilter;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.util.math.Box;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityLookup;

import java.util.Collections;
import java.util.UUID;
import java.util.function.Consumer;

public class DummyLevelEntityGetter<T extends EntityLike> implements EntityLookup<T> {

    @Override
    public T get(int p_156931_) {
        return null;
    }

    @Override
    public T get(UUID pUuid) {
        return null;
    }

    @Override
    public Iterable<T> iterate() {
        return Collections.emptyList();
    }

    @Override
    public <U extends T> void forEach(TypeFilter<T, U> p_156935_, LazyIterationConsumer<U> p_156936_) {
    }

    @Override
    public void forEachIntersects(Box p_156937_, Consumer<T> p_156938_) {
    }

    @Override
    public <U extends T> void forEachIntersects(
        TypeFilter<T, U> p_156932_,
        Box p_156933_,
        LazyIterationConsumer<U> p_156934_
    ) {
    }

}
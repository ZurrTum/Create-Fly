package com.zurrtum.create.client.flywheel.api.registry;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

@ApiStatus.NonExtendable
public interface IdRegistry<T> extends Iterable<T> {
    void register(Identifier id, T object);

    <S extends T> S registerAndGet(Identifier id, S object);

    @Nullable T get(Identifier id);

    @Nullable Identifier getId(T object);

    T getOrThrow(Identifier id);

    Identifier getIdOrThrow(T object);

    @UnmodifiableView
    Set<Identifier> getAllIds();

    @UnmodifiableView
    Collection<T> getAll();

    boolean isFrozen();
}

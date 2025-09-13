package com.zurrtum.create.client.flywheel.api.registry;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Set;

@ApiStatus.NonExtendable
public interface IdRegistry<T> extends Iterable<T> {
    void register(Identifier var1, T var2);

    <S extends T> S registerAndGet(Identifier var1, S var2);

    @Nullable T get(Identifier var1);

    @Nullable Identifier getId(T var1);

    T getOrThrow(Identifier var1);

    Identifier getIdOrThrow(T var1);

    @UnmodifiableView
    Set<Identifier> getAllIds();

    @UnmodifiableView
    Collection<T> getAll();

    boolean isFrozen();
}

package com.zurrtum.create.client.flywheel.lib.util;

import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class RendererReloadCache<T, U> implements Function<T, U> {
    private static final Set<RendererReloadCache<?, ?>> ALL = Collections.newSetFromMap(new WeakHashMap<>());
    private final Function<T, U> factory;
    private final Map<T, U> map = new ConcurrentHashMap<>();

    public RendererReloadCache(Function<T, U> factory) {
        this.factory = factory;

        synchronized (ALL) {
            ALL.add(this);
        }
    }

    public final U get(T key) {
        return map.computeIfAbsent(key, factory);
    }

    @Override
    public final U apply(T t) {
        return get(t);
    }

    public final void clear() {
        map.clear();
    }

    @ApiStatus.Internal
    public static void onReloadLevelRenderer() {
        for (RendererReloadCache<?, ?> cache : ALL) {
            cache.clear();
        }
    }
}

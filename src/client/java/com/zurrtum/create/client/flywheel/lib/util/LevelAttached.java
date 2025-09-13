package com.zurrtum.create.client.flywheel.lib.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.world.WorldAccess;

import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.function.Function;

public final class LevelAttached<T> {
    private static final ConcurrentLinkedDeque<WeakReference<LevelAttached<?>>> ALL = new ConcurrentLinkedDeque<>();
    private static final Cleaner CLEANER = Cleaner.create();

    private final LoadingCache<WorldAccess, T> cache;

    public LevelAttached(Function<WorldAccess, T> factory, Consumer<T> finalizer) {
        WeakReference<LevelAttached<?>> thisRef = new WeakReference<>(this);
        ALL.add(thisRef);

        cache = CacheBuilder.newBuilder().<WorldAccess, T>removalListener(n -> finalizer.accept(n.getValue())).build(new CacheLoader<>() {
            @Override
            public T load(WorldAccess key) {
                return factory.apply(key);
            }
        });

        CLEANER.register(this, new CleaningAction(thisRef, cache));
    }

    public LevelAttached(Function<WorldAccess, T> factory) {
        this(
            factory, t -> {
            }
        );
    }

    public static void invalidateLevel(WorldAccess level) {
        Iterator<WeakReference<LevelAttached<?>>> iterator = ALL.iterator();
        while (iterator.hasNext()) {
            LevelAttached<?> attached = iterator.next().get();
            if (attached == null) {
                iterator.remove();
            } else {
                attached.remove(level);
            }
        }
    }

    public T get(WorldAccess level) {
        return cache.getUnchecked(level);
    }

    public void remove(WorldAccess level) {
        cache.invalidate(level);
    }

    public T refresh(WorldAccess level) {
        remove(level);
        return get(level);
    }

    public void reset() {
        cache.invalidateAll();
    }

    private static class CleaningAction implements Runnable {
        private final WeakReference<LevelAttached<?>> ref;
        private final LoadingCache<WorldAccess, ?> cache;

        private CleaningAction(WeakReference<LevelAttached<?>> ref, LoadingCache<WorldAccess, ?> cache) {
            this.ref = ref;
            this.cache = cache;
        }

        @Override
        public void run() {
            ALL.remove(ref);
            cache.invalidateAll();
        }
    }
}

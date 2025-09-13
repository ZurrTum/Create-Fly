package com.zurrtum.create.catnip.data;

import net.minecraft.world.WorldAccess;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class WorldAttached<T> {

    // weak references to prevent leaking hashmaps when a WorldAttached is GC'd during runtime
    static List<WeakReference<Map<WorldAccess, ?>>> allMaps = new ArrayList<>();
    private final Map<WorldAccess, T> attached;
    private final Function<WorldAccess, T> factory;

    public WorldAttached(Function<WorldAccess, T> factory) {
        this.factory = factory;
        // Weak key hashmaps prevent worlds not existing anywhere else from leaking memory.
        // This is only a fallback in the event that unload events fail to fire for any reason.
        attached = new WeakHashMap<>();
        allMaps.add(new WeakReference<>(attached));
    }

    public static void invalidateWorld(WorldAccess world) {
        var i = allMaps.iterator();
        while (i.hasNext()) {
            Map<WorldAccess, ?> map = i.next().get();
            if (map == null) {
                // If the map has been GC'd, remove the weak reference
                i.remove();
            } else {
                // Prevent leaks
                map.remove(world);
            }
        }
    }

    public T get(WorldAccess world) {
        T t = attached.get(world);
        if (t != null)
            return t;
        T entry = factory.apply(world);
        put(world, entry);
        return entry;
    }

    public void put(WorldAccess world, T entry) {
        attached.put(world, entry);
    }

    /**
     * Replaces the entry with a new one from the factory and returns the new entry.
     */
    public T replace(WorldAccess world) {
        attached.remove(world);

        return get(world);
    }

    /**
     * Replaces the entry with a new one from the factory and returns the new entry.
     */
    public T replace(WorldAccess world, Consumer<T> finalizer) {
        T remove = attached.remove(world);

        if (remove != null)
            finalizer.accept(remove);

        return get(world);
    }

    /**
     * Deletes all entries after calling a function on them.
     *
     * @param finalizer Do something with all of the world-value pairs
     */
    public void empty(BiConsumer<WorldAccess, T> finalizer) {
        attached.forEach(finalizer);
        attached.clear();
    }

    /**
     * Deletes all entries after calling a function on them.
     *
     * @param finalizer Do something with all of the values
     */
    public void empty(Consumer<T> finalizer) {
        attached.values().forEach(finalizer);
        attached.clear();
    }
}

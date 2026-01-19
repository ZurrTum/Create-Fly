package com.zurrtum.create.api.registry;

import com.zurrtum.create.impl.registry.SimpleRegistryImpl;
import com.zurrtum.create.impl.registry.TagProviderImpl;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

/**
 * A simple registry mapping between objects with identity semantics.
 * Provides simple registration functionality, as well as lazy providers.
 * This class is thread-safe, and may be safely used during parallel mod init.
 */
@ApiStatus.NonExtendable
public interface SimpleRegistry<K, V> {
    static <K, V> SimpleRegistry<K, V> create() {
        return SimpleRegistryImpl.single();
    }

    /**
     * Register an association between a key and a value.
     * Direct registrations here always take priority over providers.
     *
     * @throws IllegalArgumentException if the object already has an associated value
     */
    void register(K object, V value);

    /**
     * Add a new provider to this registry. For information on providers, see {@link Provider}.
     *
     * @throws IllegalArgumentException if the provider has already been registered to this registry
     */
    void registerProvider(Provider<K, V> provider);

    /**
     * Invalidate the cached values provided by all providers, so they get re-computed on the next query.
     * This should be called by providers when something changes that would affect their results, such as
     * a resource reload in the case of providers based on tags.
     */
    void invalidate();

    default @Nullable V get(K object, Level world) {
        return get(object);
    }

    /**
     * Query the value associated with the given object. May be null if no association is present.
     */
    @Nullable V get(K object);

    /**
     * Shortcut for {@link #get(Object)} that accepts a StateHolder, such as BlockState or FluidState.
     */
    @Nullable V get(StateHolder<K, ?> state);

    /**
     * A provider can provide values to the registry in a lazy fashion. When a key does not have an
     * associated value, all providers will be queried in reverse-registration order (newest first).
     * <p>
     * The values returned by providers are cached so that repeated queries always return the same value.
     * To invalidate the cache of a registry, call {@link SimpleRegistry#invalidate()}.
     */
    @FunctionalInterface
    interface Provider<K, V> {
        /**
         * Create a provider that will return the same value for all entries in a tag.
         * The Provider will invalidate itself when tags are reloaded.
         */
        static <K, V> Provider<K, V> forTag(TagKey<K> tag, Function<K, Holder<K>> holderGetter, V value) {
            return new TagProviderImpl<>(tag, holderGetter, value);
        }

        /**
         * Shortcut for {@link #forTag} when the registry's type is Block.
         */
        @SuppressWarnings("deprecation")
        static <V> Provider<Block, V> forBlockTag(TagKey<Block> tag, V value) {
            return new TagProviderImpl<>(tag, Block::builtInRegistryHolder, value);
        }

        // factory methods for common Providers

        /**
         * Shortcut for {@link #forTag} when the registry's type is BlockEntityType.
         */
        static <V> Provider<BlockEntityType<?>, V> forBlockEntityTag(TagKey<BlockEntityType<?>> tag, V value) {
            return new TagProviderImpl<>(tag, TagProviderImpl::getBeHolder, value);
        }

        /**
         * Shortcut for {@link #forTag} when the registry's type is Item.
         */
        @SuppressWarnings("deprecation")
        static <V> Provider<Item, V> forItemTag(TagKey<Item> tag, V value) {
            return new TagProviderImpl<>(tag, Item::builtInRegistryHolder, value);
        }

        /**
         * Shortcut for {@link #forTag} when the registry's type is EntityType.
         */
        @SuppressWarnings("deprecation")
        static <V> Provider<EntityType<?>, V> forEntityTag(TagKey<EntityType<?>> tag, V value) {
            return new TagProviderImpl<>(tag, EntityType::builtInRegistryHolder, value);
        }

        /**
         * Shortcut for {@link #forTag} when the registry's type is Fluid.
         */
        @SuppressWarnings("deprecation")
        static <V> Provider<Fluid, V> forFluidTag(TagKey<Fluid> tag, V value) {
            return new TagProviderImpl<>(tag, Fluid::builtInRegistryHolder, value);
        }

        default @Nullable V get(K object, Level world) {
            return get(object);
        }

        @Nullable V get(K object);

        /**
         * Called by the SimpleRegistry this provider is registered to after it's registered.
         * This is useful for behavior that should only happen if a provider is actually registered,
         * such as registering event listeners.
         */
        default void onRegister(Runnable invalidate) {
        }
    }

    /**
     * An extension of SimpleRegistry that handles multiple registrations per object.
     * {@link #register(Object, Object)} Will set a whole list of registrations - use {@link #add(Object, Object)} to add one.
     * Here, all Providers are always queried, and all of their results are returned. Their provided values are also
     * provided on top of explicit registrations - they do not take priority.
     */
    interface Multi<K, V> extends SimpleRegistry<K, List<V>> {
        static <K, V> Multi<K, V> create() {
            return SimpleRegistryImpl.multi();
        }

        void add(K object, V value);

        /**
         * Shortcut that wraps a single-value provider into one that provides a List.
         */
        void addProvider(Provider<K, V> provider);

        /**
         * Never returns null, will return an empty list if no registrations are present
         */
        @Override
        List<V> get(K object);

        @Override
        List<V> get(K object, Level world);

        /**
         * Never returns null, will return an empty list if no registrations are present
         */
        @Override
        List<V> get(StateHolder<K, ?> state);
    }
}
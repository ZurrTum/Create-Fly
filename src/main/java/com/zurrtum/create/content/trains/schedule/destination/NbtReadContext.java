package com.zurrtum.create.content.trains.schedule.destination;

import com.mojang.serialization.DynamicOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.ValueInputContextHelper;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Stream;

public class NbtReadContext extends ValueInputContextHelper {
    public NbtReadContext(DynamicOps<?> ops) {
        super(new EmptyWrapperLookup(ops), null);
    }

    public static class EmptyWrapperLookup implements HolderLookup.Provider {
        private @Nullable RegistryOps<?> ops;

        public EmptyWrapperLookup(DynamicOps<?> ops) {
            if (ops instanceof RegistryOps<?> registryOps) {
                this.ops = registryOps;
            }
        }

        @Override
        public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> Optional<? extends HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> registryRef) {
            throw new UnsupportedOperationException();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <V> RegistryOps<V> createSerializationContext(DynamicOps<V> delegate) {
            return ops != null ? (RegistryOps<V>) ops : HolderLookup.Provider.super.createSerializationContext(delegate);
        }
    }
}

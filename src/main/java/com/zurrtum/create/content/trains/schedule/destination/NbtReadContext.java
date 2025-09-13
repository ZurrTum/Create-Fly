package com.zurrtum.create.content.trains.schedule.destination;

import com.mojang.serialization.DynamicOps;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadContext;

import java.util.Optional;
import java.util.stream.Stream;

public class NbtReadContext extends ReadContext {
    public NbtReadContext(DynamicOps<?> ops) {
        super(new EmptyWrapperLookup(ops), null);
    }

    public static class EmptyWrapperLookup implements RegistryWrapper.WrapperLookup {
        private RegistryOps<?> ops;

        public EmptyWrapperLookup(DynamicOps<?> ops) {
            if (ops instanceof RegistryOps<?> registryOps) {
                this.ops = registryOps;
            }
        }

        @Override
        public Stream<RegistryKey<? extends Registry<?>>> streamAllRegistryKeys() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> Optional<? extends RegistryWrapper.Impl<T>> getOptional(RegistryKey<? extends Registry<? extends T>> registryRef) {
            throw new UnsupportedOperationException();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <V> RegistryOps<V> getOps(DynamicOps<V> delegate) {
            return ops != null ? (RegistryOps<V>) ops : RegistryWrapper.WrapperLookup.super.getOps(delegate);
        }
    }
}

package com.zurrtum.create.infrastructure.resources;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryDataLoader.RegistryData;
import net.minecraft.resources.RegistryLoadTask.PendingRegistration;

import java.util.Map;
import java.util.function.Supplier;

public class NetworkLoadTask<T> {
    private final Map<Identifier, ? extends InputModify<?>> map;
    private final RegistryData<?> data;

    public NetworkLoadTask(Map<Identifier, ? extends InputModify<?>> map, RegistryData<?> data) {
        this.map = map;
        this.data = data;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Supplier<PendingRegistration<T>> replace(Identifier id, Supplier<PendingRegistration<T>> supplier) {
        InputModify<?> modify = map.get(id);
        if (modify != null) {
            return () -> ScopedValue.where(ResourceLoader.SCOPED_VALUE, new ProxyCodec(data.elementCodec(), modify))
                .call(supplier::get);
        }
        return supplier;
    }
}

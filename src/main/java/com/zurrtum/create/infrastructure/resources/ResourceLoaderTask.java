package com.zurrtum.create.infrastructure.resources;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryDataLoader.RegistryData;
import net.minecraft.server.packs.resources.Resource;

import java.util.Map;
import java.util.function.BiFunction;

public record ResourceLoaderTask<V>(Map<Identifier, ? extends InputModify<?>> map,
                                    BiFunction<Identifier, Resource, V> task,
                                    RegistryData<?> data) implements BiFunction<Identifier, Resource, V> {
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public V apply(Identifier id, Resource resource) {
        InputModify<?> modify = map.get(id);
        if (modify != null) {
            return ScopedValue.where(ResourceLoader.SCOPED_VALUE, new ProxyCodec(data.elementCodec(), modify))
                .call(() -> task.apply(id, resource));
        }
        return task.apply(id, resource);
    }
}

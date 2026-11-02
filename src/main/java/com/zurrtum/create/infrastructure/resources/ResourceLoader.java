package com.zurrtum.create.infrastructure.resources;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryDataLoader.RegistryData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class ResourceLoader {
    public static final Map<Identifier, BlockTransformerModify> TRANSFORMER = new HashMap<>();
    public static final Map<Identifier, BlockTransformerModify> TRANSFORMER_FILE = new HashMap<>();
    public static final Map<Identifier, EnchantmentModify> ENCHANTMENT_FILE = new HashMap<>();
    public static final ScopedValue<Codec<?>> SCOPED_VALUE = ScopedValue.newInstance();
    public static final Codec<?> EMPTY = Codec.EMPTY.codec();

    public static <V> BiFunction<Identifier, Resource, V> replace(
        ResourceKey<?> key,
        BiFunction<Identifier, Resource, V> operation,
        RegistryData<?> data
    ) {
        if (key == Registries.BLOCK_TRANSFORMER) {
            return new ResourceLoaderTask<>(TRANSFORMER_FILE, operation, data);
        }
        if (key == Registries.ENCHANTMENT) {
            return new ResourceLoaderTask<>(ENCHANTMENT_FILE, operation, data);
        }
        return operation;
    }

    @Nullable
    public static <T> NetworkLoadTask<T> createNetworkTask(ResourceKey<?> key, RegistryData<T> data) {
        if (key == Registries.BLOCK_TRANSFORMER) {
            return new NetworkLoadTask<>(TRANSFORMER, data);
        }
        return null;
    }

    public static BlockTransformerModify registerBlockTransformer(Identifier id, BlockTransformerModify modify) {
        TRANSFORMER.put(id, modify);
        TRANSFORMER_FILE.put(
            id.withPath(path -> Registries.BLOCK_TRANSFORMER.identifier().getPath() + "/" + path + ".json"), modify);
        return modify;
    }

    public static EnchantmentModify registerEnchantment(Identifier id, EnchantmentModify modify) {
        ENCHANTMENT_FILE.put(
            id.withPath(path -> Registries.ENCHANTMENT.identifier().getPath() + "/" + path + ".json"),
            modify
        );
        return modify;
    }
}

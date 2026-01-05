package com.zurrtum.create;

import com.mojang.serialization.Codec;
import com.zurrtum.create.api.equipment.potatoCannon.PotatoCannonProjectileType;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.List;

public class AllDynamicRegistries {
    public static final List<RegistryDataLoader.RegistryData<?>> ALL = new ArrayList<>();

    public static <T> void register(ResourceKey<Registry<T>> key, Codec<T> codec) {
        ALL.add(new RegistryDataLoader.RegistryData<>(key, codec, false));
    }

    public static void register() {
        register(CreateRegistryKeys.POTATO_PROJECTILE_TYPE, PotatoCannonProjectileType.CODEC);
    }
}

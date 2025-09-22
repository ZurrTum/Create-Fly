package com.zurrtum.create;

import com.mojang.serialization.Codec;
import com.zurrtum.create.api.equipment.potatoCannon.PotatoCannonProjectileType;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryLoader;

import java.util.ArrayList;
import java.util.List;

public class AllDynamicRegistries {
    public static boolean INITIALIZED = false;
    public static final List<RegistryLoader.Entry<?>> ALL = new ArrayList<>();

    public static <T> void register(RegistryKey<Registry<T>> key, Codec<T> codec) {
        ALL.add(new RegistryLoader.Entry<>(key, codec, false));
    }

    public static void registerIfNeeded() {
        if (!INITIALIZED) {
            register();
        }
    }

    public static void register() {
        register(CreateRegistryKeys.POTATO_PROJECTILE_TYPE, PotatoCannonProjectileType.CODEC);
        INITIALIZED = true;
    }
}

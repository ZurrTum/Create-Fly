package com.zurrtum.create;

import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;

import static com.zurrtum.create.Create.MOD_ID;

public class AllMountedItemStorageTypeTags {
    public static final TagKey<MountedItemStorageType<?>> INTERNAL = register("internal");
    public static final TagKey<MountedItemStorageType<?>> FUEL_BLACKLIST = register("fuel_blacklist");

    private static TagKey<MountedItemStorageType<?>> register(String name) {
        return TagKey.create(CreateRegistryKeys.MOUNTED_ITEM_STORAGE_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, name));
    }

    public static void register() {
    }
}

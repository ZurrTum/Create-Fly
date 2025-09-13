package com.zurrtum.create;

import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllMountedItemStorageTypeTags {
    public static final TagKey<MountedItemStorageType<?>> INTERNAL = register("internal");
    public static final TagKey<MountedItemStorageType<?>> FUEL_BLACKLIST = register("fuel_blacklist");

    private static TagKey<MountedItemStorageType<?>> register(String name) {
        return TagKey.of(CreateRegistryKeys.MOUNTED_ITEM_STORAGE_TYPE, Identifier.of(MOD_ID, name));
    }

    public static void register() {
    }
}

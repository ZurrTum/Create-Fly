package com.zurrtum.create;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllEntityTags {
    public static final TagKey<EntityType<?>> BLAZE_BURNER_CAPTURABLE = register("blaze_burner_capturable");
    public static final TagKey<EntityType<?>> IGNORE_SEAT = register("ignore_seat");

    private static TagKey<EntityType<?>> register(String name) {
        return TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, name));
    }

    public static void register() {
    }
}

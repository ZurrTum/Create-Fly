package com.zurrtum.create;

import static com.zurrtum.create.Create.MOD_ID;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class AllEntityTags {
    public static final TagKey<EntityType<?>> BLAZE_BURNER_CAPTURABLE = register("blaze_burner_capturable");
    public static final TagKey<EntityType<?>> IGNORE_SEAT = register("ignore_seat");

    private static TagKey<EntityType<?>> register(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, name));
    }

    public static void register() {
    }
}

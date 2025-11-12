package com.zurrtum.create;

import com.zurrtum.create.api.contraption.ContraptionType;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import static com.zurrtum.create.Create.MOD_ID;

public class AllContraptionTypeTags {
    public static final TagKey<ContraptionType> OPENS_CONTROLS = register("opens_controls");
    public static final TagKey<ContraptionType> REQUIRES_VEHICLE_FOR_RENDER = register("requires_vehicle_for_render");

    private static TagKey<ContraptionType> register(String name) {
        return TagKey.create(CreateRegistryKeys.CONTRAPTION_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, name));
    }

    public static void register() {
    }
}

package com.zurrtum.create;

import com.zurrtum.create.api.contraption.ContraptionType;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllContraptionTypeTags {
    public static final TagKey<ContraptionType> OPENS_CONTROLS = register("opens_controls");
    public static final TagKey<ContraptionType> REQUIRES_VEHICLE_FOR_RENDER = register("requires_vehicle_for_render");

    private static TagKey<ContraptionType> register(String name) {
        return TagKey.of(CreateRegistryKeys.CONTRAPTION_TYPE, Identifier.of(MOD_ID, name));
    }

    public static void register() {
    }
}

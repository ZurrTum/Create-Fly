package com.zurrtum.create.infrastructure.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllPlacementModifiers {
    public static final Identifier CONFIG_FILTER = register("config_filter", ConfigPlacementFilter.CODEC);

    private static <P extends PlacementModifier> Identifier register(String name, MapCodec<P> codec) {
        Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, name);
        Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, id, codec);
        return id;
    }

    public static void register() {
    }
}

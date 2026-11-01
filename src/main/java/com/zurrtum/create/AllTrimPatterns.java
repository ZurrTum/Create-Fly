package com.zurrtum.create;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.trim.TrimPattern;

import static com.zurrtum.create.Create.MOD_ID;

public class AllTrimPatterns {
    public static final ResourceKey<TrimPattern> SENTRY = registry("sentry");
    public static final ResourceKey<TrimPattern> DUNE = registry("dune");
    public static final ResourceKey<TrimPattern> COAST = registry("coast");
    public static final ResourceKey<TrimPattern> WILD = registry("wild");
    public static final ResourceKey<TrimPattern> WARD = registry("ward");
    public static final ResourceKey<TrimPattern> EYE = registry("eye");
    public static final ResourceKey<TrimPattern> VEX = registry("vex");
    public static final ResourceKey<TrimPattern> TIDE = registry("tide");
    public static final ResourceKey<TrimPattern> SNOUT = registry("snout");
    public static final ResourceKey<TrimPattern> RIB = registry("rib");
    public static final ResourceKey<TrimPattern> SPIRE = registry("spire");
    public static final ResourceKey<TrimPattern> WAYFINDER = registry("wayfinder");
    public static final ResourceKey<TrimPattern> SHAPER = registry("shaper");
    public static final ResourceKey<TrimPattern> SILENCE = registry("silence");
    public static final ResourceKey<TrimPattern> RAISER = registry("raiser");
    public static final ResourceKey<TrimPattern> HOST = registry("host");
    public static final ResourceKey<TrimPattern> FLOW = registry("flow");
    public static final ResourceKey<TrimPattern> BOLT = registry("bolt");

    private static ResourceKey<TrimPattern> registry(final String id) {
        return ResourceKey.create(Registries.TRIM_PATTERN, Identifier.fromNamespaceAndPath(MOD_ID, id));
    }

    public static void register() {
    }
}

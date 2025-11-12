package com.zurrtum.create;

import static com.zurrtum.create.Create.MOD_ID;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class AllFluidTags {
    public static final TagKey<Fluid> BOTTOMLESS_ALLOW = register("bottomless/allow");
    public static final TagKey<Fluid> BOTTOMLESS_DENY = register("bottomless/deny");
    public static final TagKey<Fluid> FAN_PROCESSING_CATALYSTS_BLASTING = register("fan_processing_catalysts/blasting");
    public static final TagKey<Fluid> FAN_PROCESSING_CATALYSTS_HAUNTING = register("fan_processing_catalysts/haunting");
    public static final TagKey<Fluid> FAN_PROCESSING_CATALYSTS_SMOKING = register("fan_processing_catalysts/smoking");
    public static final TagKey<Fluid> FAN_PROCESSING_CATALYSTS_SPLASHING = register("fan_processing_catalysts/splashing");
    public static final TagKey<Fluid> MILK = register("c", "milk");
    public static final TagKey<Fluid> TEA = register("c", "tea");
    public static final TagKey<Fluid> CHOCOLATE = register("c", "chocolate");
    public static final TagKey<Fluid> CREOSOTE = register("c", "creosote");

    private static TagKey<Fluid> register(String name) {
        return TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(MOD_ID, name));
    }

    private static TagKey<Fluid> register(String namespace, String name) {
        return TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(namespace, name));
    }

    public static void register() {
    }
}

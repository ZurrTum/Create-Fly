package com.zurrtum.create;

import net.minecraft.fluid.Fluid;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllFluidTags {
    public static final TagKey<Fluid> BOTTOMLESS_ALLOW = register("bottomless/allow");
    public static final TagKey<Fluid> BOTTOMLESS_DENY = register("bottomless/deny");
    public static final TagKey<Fluid> FAN_PROCESSING_CATALYSTS_BLASTING = register("fan_processing_catalysts/blasting");
    public static final TagKey<Fluid> FAN_PROCESSING_CATALYSTS_HAUNTING = register("fan_processing_catalysts/haunting");
    public static final TagKey<Fluid> FAN_PROCESSING_CATALYSTS_SMOKING = register("fan_processing_catalysts/smoking");
    public static final TagKey<Fluid> FAN_PROCESSING_CATALYSTS_SPLASHING = register("fan_processing_catalysts/splashing");
    public static final TagKey<Fluid> MILK = register("milk");

    private static TagKey<Fluid> register(String name) {
        return TagKey.of(RegistryKeys.FLUID, Identifier.of(MOD_ID, name));
    }

    private static TagKey<Fluid> register(String namespace, String name) {
        return TagKey.of(RegistryKeys.FLUID, Identifier.of(namespace, name));
    }

    public static void register() {
    }
}

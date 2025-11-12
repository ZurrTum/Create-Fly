package com.zurrtum.create;

import static com.zurrtum.create.Create.MOD_ID;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public class AllDamageTypes {
    public static final ResourceKey<DamageType> CRUSH = register("crush");
    public static final ResourceKey<DamageType> CUCKOO_SURPRISE = register("cuckoo_surprise");
    public static final ResourceKey<DamageType> FAN_FIRE = register("fan_fire");
    public static final ResourceKey<DamageType> FAN_LAVA = register("fan_lava");
    public static final ResourceKey<DamageType> DRILL = register("mechanical_drill");
    public static final ResourceKey<DamageType> ROLLER = register("mechanical_roller");
    public static final ResourceKey<DamageType> SAW = register("mechanical_saw");
    public static final ResourceKey<DamageType> POTATO_CANNON = register("potato_cannon");
    public static final ResourceKey<DamageType> RUN_OVER = register("run_over");

    private static ResourceKey<DamageType> register(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, name));
    }

    public static void register() {
    }
}

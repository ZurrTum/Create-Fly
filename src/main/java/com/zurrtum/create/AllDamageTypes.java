package com.zurrtum.create;

import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllDamageTypes {
    public static final RegistryKey<DamageType> CRUSH = register("crush");
    public static final RegistryKey<DamageType> CUCKOO_SURPRISE = register("cuckoo_surprise");
    public static final RegistryKey<DamageType> FAN_FIRE = register("fan_fire");
    public static final RegistryKey<DamageType> FAN_LAVA = register("fan_lava");
    public static final RegistryKey<DamageType> DRILL = register("mechanical_drill");
    public static final RegistryKey<DamageType> ROLLER = register("mechanical_roller");
    public static final RegistryKey<DamageType> SAW = register("mechanical_saw");
    public static final RegistryKey<DamageType> POTATO_CANNON = register("potato_cannon");
    public static final RegistryKey<DamageType> RUN_OVER = register("run_over");

    private static RegistryKey<DamageType> register(String name) {
        return RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(MOD_ID, name));
    }

    public static void register() {
    }
}

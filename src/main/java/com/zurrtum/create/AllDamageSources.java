package com.zurrtum.create;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

import java.util.IdentityHashMap;
import java.util.Map;

public class AllDamageSources {
    private static final Map<DynamicRegistryManager, AllDamageSources> ALL = new IdentityHashMap<>();

    public static AllDamageSources get(DynamicRegistryManager registryManager) {
        return ALL.get(registryManager);
    }

    public static AllDamageSources get(World world) {
        return ALL.get(world.getRegistryManager());
    }

    public Registry<DamageType> registry;
    public DamageSource crush;
    public DamageSource cuckoo_surprise;
    public DamageSource fan_fire;
    public DamageSource fan_lava;
    public DamageSource drill;
    public DamageSource roller;
    public DamageSource saw;

    public AllDamageSources(DynamicRegistryManager registryManager) {
        registry = registryManager.getOrThrow(RegistryKeys.DAMAGE_TYPE);
        crush = create(AllDamageTypes.CRUSH);
        cuckoo_surprise = create(AllDamageTypes.CUCKOO_SURPRISE);
        fan_fire = create(AllDamageTypes.FAN_FIRE);
        fan_lava = create(AllDamageTypes.FAN_LAVA);
        drill = create(AllDamageTypes.DRILL);
        roller = create(AllDamageTypes.ROLLER);
        saw = create(AllDamageTypes.SAW);
    }

    public DamageSource create(RegistryKey<DamageType> type) {
        return new DamageSource(registry.getOrThrow(type));
    }

    public DamageSource potatoCannon(Entity causingEntity, Entity directEntity) {
        return new DamageSource(registry.getOrThrow(AllDamageTypes.POTATO_CANNON), causingEntity, directEntity);
    }

    public DamageSource runOver(Entity entity) {
        return new DamageSource(registry.getOrThrow(AllDamageTypes.RUN_OVER), entity);
    }

    public static void register(DynamicRegistryManager registryManager) {
        ALL.put(registryManager, new AllDamageSources(registryManager));
    }
}

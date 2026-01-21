package com.zurrtum.create;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

public class AllDamageSources {
    private static final Map<RegistryAccess, AllDamageSources> ALL = new IdentityHashMap<>();

    public static AllDamageSources get(RegistryAccess registryManager) {
        return ALL.get(registryManager);
    }

    public static AllDamageSources get(Level world) {
        return ALL.get(world.registryAccess());
    }

    public Registry<DamageType> registry;
    public DamageSource crush;
    public DamageSource cuckoo_surprise;
    public DamageSource fan_fire;
    public DamageSource fan_lava;
    public DamageSource drill;
    public DamageSource roller;
    public DamageSource saw;

    public AllDamageSources(RegistryAccess registryManager) {
        registryManager.lookup(Registries.DAMAGE_TYPE).ifPresent(value -> {
            registry = value;
            crush = create(AllDamageTypes.CRUSH);
            cuckoo_surprise = create(AllDamageTypes.CUCKOO_SURPRISE);
            fan_fire = create(AllDamageTypes.FAN_FIRE);
            fan_lava = create(AllDamageTypes.FAN_LAVA);
            drill = create(AllDamageTypes.DRILL);
            roller = create(AllDamageTypes.ROLLER);
            saw = create(AllDamageTypes.SAW);
        });
    }

    @Nullable
    public DamageSource create(ResourceKey<DamageType> type) {
        return registry.get(type).map(DamageSource::new).orElse(null);
    }

    public DamageSource potatoCannon(Entity causingEntity, @Nullable Entity directEntity) {
        return new DamageSource(registry.getOrThrow(AllDamageTypes.POTATO_CANNON), causingEntity, directEntity);
    }

    public DamageSource runOver(Entity entity) {
        return new DamageSource(registry.getOrThrow(AllDamageTypes.RUN_OVER), entity);
    }

    public static void register(RegistryAccess registryManager) {
        ALL.put(registryManager, new AllDamageSources(registryManager));
    }
}

package com.zurrtum.create;

import com.zurrtum.create.content.logistics.box.PackageEntity;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

public class AllEntityAttributes {
    public static boolean INITIALIZED = false;
    public static final Map<EntityType<? extends LivingEntity>, Supplier<AttributeSupplier.Builder>> ATTRIBUTES = new IdentityHashMap<>();

    public static void registerIfNeeded() {
        if (!INITIALIZED) {
            register();
        }
    }

    public static void register() {
        ATTRIBUTES.put(AllEntityTypes.PACKAGE, PackageEntity::createPackageAttributes);
        INITIALIZED = true;
    }
}

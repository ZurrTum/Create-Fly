package com.zurrtum.create;

import com.zurrtum.create.content.logistics.box.PackageEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AllEntityAttributes {
    public static boolean INITIALIZED = false;
    public static final Map<EntityType<? extends LivingEntity>, Supplier<DefaultAttributeContainer.Builder>> ATTRIBUTES = new IdentityHashMap<>();

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

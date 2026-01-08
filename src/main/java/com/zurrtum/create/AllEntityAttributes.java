package com.zurrtum.create;

import com.zurrtum.create.content.logistics.box.PackageEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AllEntityAttributes {
    public static final Map<EntityType<? extends LivingEntity>, Supplier<AttributeSupplier.Builder>> ATTRIBUTES = new IdentityHashMap<>();

    public static void register() {
        ATTRIBUTES.put(AllEntityTypes.PACKAGE, PackageEntity::createPackageAttributes);
    }
}

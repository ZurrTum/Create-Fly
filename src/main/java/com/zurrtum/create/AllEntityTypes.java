package com.zurrtum.create;

import com.zurrtum.create.content.contraptions.ControlledContraptionEntity;
import com.zurrtum.create.content.contraptions.OrientedContraptionEntity;
import com.zurrtum.create.content.contraptions.actors.seat.SeatEntity;
import com.zurrtum.create.content.contraptions.gantry.GantryContraptionEntity;
import com.zurrtum.create.content.contraptions.glue.SuperGlueEntity;
import com.zurrtum.create.content.equipment.blueprint.BlueprintEntity;
import com.zurrtum.create.content.equipment.potatoCannon.PotatoProjectileEntity;
import com.zurrtum.create.content.logistics.box.PackageEntity;
import com.zurrtum.create.content.logistics.depot.EjectorItemEntity;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllEntityTypes {
    public static final Set<EntityType<?>> NOT_SEND_VELOCITY = new HashSet<>();
    public static final Map<EntityType<? extends LivingEntity>, Supplier<DefaultAttributeContainer.Builder>> ATTRIBUTES = new IdentityHashMap<>();
    public static final EntityType<EjectorItemEntity> EJECTOR_ITEM = register(
        "ejector_item",
        EntityType.Builder.<EjectorItemEntity>create(EjectorItemEntity::new, SpawnGroup.MISC).dropsNothing().dimensions(0.25F, 0.25F)
            .eyeHeight(0.2125F).maxTrackingRange(6).trackingTickInterval(20)
    );
    public static final EntityType<OrientedContraptionEntity> ORIENTED_CONTRAPTION = register(
        "contraption",
        EntityType.Builder.create(OrientedContraptionEntity::new, SpawnGroup.MISC).dimensions(1, 1).makeFireImmune()
    );
    public static final EntityType<ControlledContraptionEntity> CONTROLLED_CONTRAPTION = register(
        "stationary_contraption",
        EntityType.Builder.create(ControlledContraptionEntity::new, SpawnGroup.MISC).dimensions(1, 1).maxTrackingRange(20).trackingTickInterval(40)
            .makeFireImmune()
    );
    public static final EntityType<CarriageContraptionEntity> CARRIAGE_CONTRAPTION = register(
        "carriage_contraption",
        EntityType.Builder.create(CarriageContraptionEntity::new, SpawnGroup.MISC).dimensions(1, 1).maxTrackingRange(15).makeFireImmune()
    );
    public static final EntityType<SuperGlueEntity> SUPER_GLUE = register(
        "super_glue",
        EntityType.Builder.<SuperGlueEntity>create(SuperGlueEntity::new, SpawnGroup.MISC).maxTrackingRange(10).trackingTickInterval(Integer.MAX_VALUE)
            .makeFireImmune()
    );
    public static final EntityType<GantryContraptionEntity> GANTRY_CONTRAPTION = register(
        "gantry_contraption",
        EntityType.Builder.create(GantryContraptionEntity::new, SpawnGroup.MISC).dimensions(1, 1).maxTrackingRange(10).trackingTickInterval(40)
            .makeFireImmune()
    );
    public static final EntityType<SeatEntity> SEAT = register(
        "seat",
        EntityType.Builder.<SeatEntity>create(SeatEntity::new, SpawnGroup.MISC).trackingTickInterval(Integer.MAX_VALUE).makeFireImmune()
            .dimensions(0.25f, 0.35f)
    );
    public static final EntityType<PotatoProjectileEntity> POTATO_PROJECTILE = register(
        "potato_projectile",
        EntityType.Builder.create(PotatoProjectileEntity::new, SpawnGroup.MISC).maxTrackingRange(4).trackingTickInterval(20).dimensions(.25f, .25f)
    );
    public static final EntityType<PackageEntity> PACKAGE = register(
        "package",
        EntityType.Builder.<PackageEntity>create(PackageEntity::new, SpawnGroup.MISC).maxTrackingRange(10).trackingTickInterval(3).dimensions(1, 1)
    );
    public static final EntityType<BlueprintEntity> CRAFTING_BLUEPRINT = register(
        "crafting_blueprint",
        EntityType.Builder.<BlueprintEntity>create(BlueprintEntity::new, SpawnGroup.MISC).maxTrackingRange(10).trackingTickInterval(Integer.MAX_VALUE)
            .makeFireImmune()
    );

    private static <T extends Entity> EntityType<T> register(String id, EntityType.Builder<T> type) {
        RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, id));
        return Registry.register(Registries.ENTITY_TYPE, key, type.build(key));
    }

    public static void register() {
        NOT_SEND_VELOCITY.add(SUPER_GLUE);
        NOT_SEND_VELOCITY.add(CONTROLLED_CONTRAPTION);
        NOT_SEND_VELOCITY.add(GANTRY_CONTRAPTION);
        NOT_SEND_VELOCITY.add(SEAT);
        NOT_SEND_VELOCITY.add(CRAFTING_BLUEPRINT);
        ATTRIBUTES.put(PACKAGE, PackageEntity::createPackageAttributes);
    }
}

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

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import static com.zurrtum.create.Create.MOD_ID;

public class AllEntityTypes {
    public static final Set<EntityType<?>> NOT_SEND_VELOCITY = new HashSet<>();
    public static final EntityType<EjectorItemEntity> EJECTOR_ITEM = register(
        "ejector_item",
        EntityType.Builder.<EjectorItemEntity>of(EjectorItemEntity::new, MobCategory.MISC).noLootTable().sized(0.25F, 0.25F).eyeHeight(0.2125F)
            .clientTrackingRange(6).updateInterval(20)
    );
    public static final EntityType<OrientedContraptionEntity> ORIENTED_CONTRAPTION = register(
        "contraption",
        EntityType.Builder.of(OrientedContraptionEntity::new, MobCategory.MISC).sized(1, 1).fireImmune()
    );
    public static final EntityType<ControlledContraptionEntity> CONTROLLED_CONTRAPTION = register(
        "stationary_contraption",
        EntityType.Builder.of(ControlledContraptionEntity::new, MobCategory.MISC).sized(1, 1).clientTrackingRange(20).updateInterval(40).fireImmune()
    );
    public static final EntityType<CarriageContraptionEntity> CARRIAGE_CONTRAPTION = register(
        "carriage_contraption",
        EntityType.Builder.of(CarriageContraptionEntity::new, MobCategory.MISC).sized(1, 1).clientTrackingRange(15).fireImmune()
    );
    public static final EntityType<SuperGlueEntity> SUPER_GLUE = register(
        "super_glue",
        EntityType.Builder.<SuperGlueEntity>of(SuperGlueEntity::new, MobCategory.MISC).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE)
            .fireImmune()
    );
    public static final EntityType<GantryContraptionEntity> GANTRY_CONTRAPTION = register(
        "gantry_contraption",
        EntityType.Builder.of(GantryContraptionEntity::new, MobCategory.MISC).sized(1, 1).clientTrackingRange(10).updateInterval(40).fireImmune()
    );
    public static final EntityType<SeatEntity> SEAT = register(
        "seat",
        EntityType.Builder.<SeatEntity>of(SeatEntity::new, MobCategory.MISC).updateInterval(Integer.MAX_VALUE).fireImmune().sized(0.25f, 0.35f)
    );
    public static final EntityType<PotatoProjectileEntity> POTATO_PROJECTILE = register(
        "potato_projectile",
        EntityType.Builder.of(PotatoProjectileEntity::new, MobCategory.MISC).clientTrackingRange(4).updateInterval(20).sized(.25f, .25f)
    );
    public static final EntityType<PackageEntity> PACKAGE = register(
        "package",
        EntityType.Builder.<PackageEntity>of(PackageEntity::new, MobCategory.MISC).clientTrackingRange(10).updateInterval(3).sized(1, 1)
    );
    public static final EntityType<BlueprintEntity> CRAFTING_BLUEPRINT = register(
        "crafting_blueprint",
        EntityType.Builder.<BlueprintEntity>of(BlueprintEntity::new, MobCategory.MISC).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE)
            .fireImmune()
    );

    private static <T extends Entity> EntityType<T> register(String id, EntityType.Builder<T> type) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, id));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type.build(key));
    }

    public static void register() {
        NOT_SEND_VELOCITY.add(SUPER_GLUE);
        NOT_SEND_VELOCITY.add(CONTROLLED_CONTRAPTION);
        NOT_SEND_VELOCITY.add(GANTRY_CONTRAPTION);
        NOT_SEND_VELOCITY.add(SEAT);
        NOT_SEND_VELOCITY.add(CRAFTING_BLUEPRINT);
    }
}

package com.zurrtum.create.api.registry;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.api.behaviour.display.DisplayTarget;
import com.zurrtum.create.api.contraption.ContraptionType;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import com.zurrtum.create.api.equipment.potatoCannon.PotatoCannonProjectileType;
import com.zurrtum.create.api.equipment.potatoCannon.PotatoProjectileBlockHitAction;
import com.zurrtum.create.api.equipment.potatoCannon.PotatoProjectileEntityHitAction;
import com.zurrtum.create.api.equipment.potatoCannon.PotatoProjectileRenderMode;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessingType;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.zurrtum.create.content.logistics.packagePort.PackagePortTargetType;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class CreateRegistryKeys {
    public static final RegistryKey<Registry<ArmInteractionPointType>> ARM_INTERACTION_POINT_TYPE = register("arm_interaction_point_type");
    public static final RegistryKey<Registry<FanProcessingType>> FAN_PROCESSING_TYPE = register("fan_processing_type");
    public static final RegistryKey<Registry<ItemAttributeType>> ITEM_ATTRIBUTE_TYPE = register("item_attribute_type");
    public static final RegistryKey<Registry<DisplaySource>> DISPLAY_SOURCE = register("display_source");
    public static final RegistryKey<Registry<DisplayTarget>> DISPLAY_TARGET = register("display_target");
    public static final RegistryKey<Registry<MountedItemStorageType<?>>> MOUNTED_ITEM_STORAGE_TYPE = register("mounted_item_storage_type");
    public static final RegistryKey<Registry<MountedFluidStorageType<?>>> MOUNTED_FLUID_STORAGE_TYPE = register("mounted_fluid_storage_type");
    public static final RegistryKey<Registry<ContraptionType>> CONTRAPTION_TYPE = register("contraption_type");
    public static final RegistryKey<Registry<PotatoCannonProjectileType>> POTATO_PROJECTILE_TYPE = register("potato_projectile/type");
    public static final RegistryKey<Registry<MapCodec<? extends PotatoProjectileRenderMode>>> POTATO_PROJECTILE_RENDER_MODE = register(
        "potato_projectile/render_mode");
    public static final RegistryKey<Registry<MapCodec<? extends PotatoProjectileEntityHitAction>>> POTATO_PROJECTILE_ENTITY_HIT_ACTION = register(
        "potato_projectile/entity_hit_action");
    public static final RegistryKey<Registry<MapCodec<? extends PotatoProjectileBlockHitAction>>> POTATO_PROJECTILE_BLOCK_HIT_ACTION = register(
        "potato_projectile/block_hit_action");
    public static final RegistryKey<Registry<PackagePortTargetType>> PACKAGE_PORT_TARGET_TYPE = register("package_port_target_type");
    public static final RegistryKey<Registry<MenuType<?>>> MENU_TYPE = register("menu_type");

    private static <T> RegistryKey<Registry<T>> register(String id) {
        return RegistryKey.ofRegistry(Identifier.of(MOD_ID, id));
    }

    public static void register() {
    }
}

package com.zurrtum.create.api.registry;

import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.api.behaviour.display.DisplayTarget;
import com.zurrtum.create.api.contraption.ContraptionType;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import com.zurrtum.create.api.equipment.potatoCannon.PotatoProjectileBlockHitAction;
import com.zurrtum.create.api.equipment.potatoCannon.PotatoProjectileEntityHitAction;
import com.zurrtum.create.api.equipment.potatoCannon.PotatoProjectileRenderMode;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessingType;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.zurrtum.create.content.logistics.packagePort.PackagePortTargetType;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import net.minecraft.registry.*;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntryInfo;

public class CreateRegistries {
    public static final Registry<ArmInteractionPointType> ARM_INTERACTION_POINT_TYPE = register(CreateRegistryKeys.ARM_INTERACTION_POINT_TYPE);
    public static final Registry<FanProcessingType> FAN_PROCESSING_TYPE = register(CreateRegistryKeys.FAN_PROCESSING_TYPE);
    public static final Registry<ItemAttributeType> ITEM_ATTRIBUTE_TYPE = register(CreateRegistryKeys.ITEM_ATTRIBUTE_TYPE);
    public static final Registry<DisplaySource> DISPLAY_SOURCE = register(CreateRegistryKeys.DISPLAY_SOURCE);
    public static final Registry<DisplayTarget> DISPLAY_TARGET = register(CreateRegistryKeys.DISPLAY_TARGET);
    public static final Registry<MountedItemStorageType<?>> MOUNTED_ITEM_STORAGE_TYPE = registerIntrusive(CreateRegistryKeys.MOUNTED_ITEM_STORAGE_TYPE);
    public static final Registry<MountedFluidStorageType<?>> MOUNTED_FLUID_STORAGE_TYPE = register(CreateRegistryKeys.MOUNTED_FLUID_STORAGE_TYPE);
    public static final Registry<ContraptionType> CONTRAPTION_TYPE = registerIntrusive(CreateRegistryKeys.CONTRAPTION_TYPE);
    public static final Registry<MapCodec<? extends PotatoProjectileRenderMode>> POTATO_PROJECTILE_RENDER_MODE = register(CreateRegistryKeys.POTATO_PROJECTILE_RENDER_MODE);
    public static final Registry<MapCodec<? extends PotatoProjectileEntityHitAction>> POTATO_PROJECTILE_ENTITY_HIT_ACTION = register(
        CreateRegistryKeys.POTATO_PROJECTILE_ENTITY_HIT_ACTION);
    public static final Registry<MapCodec<? extends PotatoProjectileBlockHitAction>> POTATO_PROJECTILE_BLOCK_HIT_ACTION = register(CreateRegistryKeys.POTATO_PROJECTILE_BLOCK_HIT_ACTION);
    public static final Registry<PackagePortTargetType> PACKAGE_PORT_TARGET_TYPE = register(CreateRegistryKeys.PACKAGE_PORT_TARGET_TYPE);
    public static final Registry<MenuType<?>> MENU_TYPE = register(CreateRegistryKeys.MENU_TYPE);

    private static <T> Registry<T> register(RegistryKey<? extends Registry<T>> key) {
        return register(key, false);
    }

    private static <T> Registry<T> registerIntrusive(RegistryKey<? extends Registry<T>> key) {
        return register(key, true);
    }

    @SuppressWarnings("unchecked")
    private static <T> Registry<T> register(RegistryKey<? extends Registry<T>> key, boolean intrusive) {
        SimpleRegistry<T> registry = new SimpleRegistry<>(key, Lifecycle.stable(), intrusive);
        Registries.ROOT.add((RegistryKey<MutableRegistry<?>>) (Object) key, registry, RegistryEntryInfo.DEFAULT);
        return registry;
    }

    public static void register() {
    }
}
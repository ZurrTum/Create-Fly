package com.zurrtum.create.content.equipment.armor;

import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllEquipmentAssetKeys {
    public static final RegistryKey<EquipmentAsset> COPPER = register("copper");
    public static final RegistryKey<EquipmentAsset> NETHERITE = register("netherite");
    public static final RegistryKey<EquipmentAsset> CARDBOARD = register("cardboard");

    private static RegistryKey<EquipmentAsset> register(String name) {
        return RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, Identifier.of(MOD_ID, name));
    }

    public static void register() {
    }
}

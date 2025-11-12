package com.zurrtum.create.content.equipment.armor;

import static com.zurrtum.create.Create.MOD_ID;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

public class AllEquipmentAssetKeys {
    public static final ResourceKey<EquipmentAsset> COPPER = register("copper");
    public static final ResourceKey<EquipmentAsset> NETHERITE = register("netherite");
    public static final ResourceKey<EquipmentAsset> CARDBOARD = register("cardboard");

    private static ResourceKey<EquipmentAsset> register(String name) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, ResourceLocation.fromNamespaceAndPath(MOD_ID, name));
    }

    public static void register() {
    }
}

package com.zurrtum.create.content.equipment.armor;

import com.google.common.collect.Maps;
import com.zurrtum.create.AllItemTags;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.Map;

public class AllArmorMaterials {
    public static final ArmorMaterial COPPER = register(
        7,
        createDefenseMap(1, 3, 4, 2, 4),
        7,
        SoundEvents.ITEM_ARMOR_EQUIP_GOLD,
        0.0F,
        0.0F,
        AllItemTags.REPAIRS_COPPER_ARMOR,
        AllEquipmentAssetKeys.COPPER
    );
    public static final ArmorMaterial CARDBOARD = register(
        4,
        createDefenseMap(1, 1, 1, 1, 2),
        4,
        SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
        0.0F,
        0.0F,
        AllItemTags.REPAIRS_CARDBOARD_ARMOR,
        AllEquipmentAssetKeys.CARDBOARD
    );
    public static final ArmorMaterial NETHERITE = register(
        37,
        createDefenseMap(3, 6, 8, 3, 11),
        15,
        SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE,
        3.0F,
        0.1F,
        ItemTags.REPAIRS_NETHERITE_ARMOR,
        AllEquipmentAssetKeys.NETHERITE
    );

    private static ArmorMaterial register(
        int durability,
        Map<EquipmentType, Integer> defense,
        int enchantmentValue,
        RegistryEntry<SoundEvent> equipSound,
        float toughness,
        float knockbackResistance,
        TagKey<Item> repairIngredient,
        RegistryKey<EquipmentAsset> assetId
    ) {
        return new ArmorMaterial(durability, defense, enchantmentValue, equipSound, toughness, knockbackResistance, repairIngredient, assetId);
    }

    private static Map<EquipmentType, Integer> createDefenseMap(
        int bootsDefense,
        int leggingsDefense,
        int chestplateDefense,
        int helmetDefense,
        int bodyDefense
    ) {
        return Maps.newEnumMap(Map.of(
            EquipmentType.BOOTS,
            bootsDefense,
            EquipmentType.LEGGINGS,
            leggingsDefense,
            EquipmentType.CHESTPLATE,
            chestplateDefense,
            EquipmentType.HELMET,
            helmetDefense,
            EquipmentType.BODY,
            bodyDefense
        ));
    }

    public static Item.Settings chest(ArmorMaterial material) {
        return new Item.Settings().attributeModifiers(material.createAttributeModifiers(EquipmentType.CHESTPLATE))
            .enchantable(material.enchantmentValue()).repairable(material.repairIngredient()).component(
                DataComponentTypes.EQUIPPABLE,
                EquippableComponent.builder(EquipmentSlot.CHEST).equipSound(material.equipSound()).model(material.assetId()).build()
            );
    }

    public static void register() {
    }
}

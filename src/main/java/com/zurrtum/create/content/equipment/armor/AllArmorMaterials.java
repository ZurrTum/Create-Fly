package com.zurrtum.create.content.equipment.armor;

import com.google.common.collect.Maps;
import com.zurrtum.create.AllItemTags;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;

public class AllArmorMaterials {
    public static final ArmorMaterial COPPER = register(
        7,
        createDefenseMap(1, 3, 4, 2, 4),
        7,
        SoundEvents.ARMOR_EQUIP_GOLD,
        0.0F,
        0.0F,
        AllItemTags.REPAIRS_COPPER_ARMOR,
        AllEquipmentAssetKeys.COPPER
    );
    public static final ArmorMaterial CARDBOARD = register(
        4,
        createDefenseMap(1, 1, 1, 1, 2),
        4,
        SoundEvents.ARMOR_EQUIP_LEATHER,
        0.0F,
        0.0F,
        AllItemTags.REPAIRS_CARDBOARD_ARMOR,
        AllEquipmentAssetKeys.CARDBOARD
    );
    public static final ArmorMaterial NETHERITE = register(
        37,
        createDefenseMap(3, 6, 8, 3, 11),
        15,
        SoundEvents.ARMOR_EQUIP_NETHERITE,
        3.0F,
        0.1F,
        ItemTags.REPAIRS_NETHERITE_ARMOR,
        AllEquipmentAssetKeys.NETHERITE
    );

    private static ArmorMaterial register(
        int durability,
        Map<ArmorType, Integer> defense,
        int enchantmentValue,
        Holder<SoundEvent> equipSound,
        float toughness,
        float knockbackResistance,
        TagKey<Item> repairIngredient,
        ResourceKey<EquipmentAsset> assetId
    ) {
        return new ArmorMaterial(durability, defense, enchantmentValue, equipSound, toughness, knockbackResistance, repairIngredient, assetId);
    }

    private static Map<ArmorType, Integer> createDefenseMap(
        int bootsDefense,
        int leggingsDefense,
        int chestplateDefense,
        int helmetDefense,
        int bodyDefense
    ) {
        return Maps.newEnumMap(Map.of(
            ArmorType.BOOTS,
            bootsDefense,
            ArmorType.LEGGINGS,
            leggingsDefense,
            ArmorType.CHESTPLATE,
            chestplateDefense,
            ArmorType.HELMET,
            helmetDefense,
            ArmorType.BODY,
            bodyDefense
        ));
    }

    public static Item.Properties chest(ArmorMaterial material) {
        return new Item.Properties().attributes(material.createAttributes(ArmorType.CHESTPLATE))
            .enchantable(material.enchantmentValue()).repairable(material.repairIngredient()).component(
                DataComponents.EQUIPPABLE,
                Equippable.builder(EquipmentSlot.CHEST).setEquipSound(material.equipSound()).setAsset(material.assetId()).build()
            );
    }

    public static void register() {
    }
}

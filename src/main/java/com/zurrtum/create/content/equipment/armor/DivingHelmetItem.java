package com.zurrtum.create.content.equipment.armor;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.AllAdvancements;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

import static com.zurrtum.create.Create.MOD_ID;

public class DivingHelmetItem extends Item {
    public static final EquipmentSlot SLOT = EquipmentSlot.HEAD;
    public static final AttributeModifier SPEED_MODIFIER = new AttributeModifier(
        ResourceLocation.fromNamespaceAndPath(MOD_ID, "netherite_diving_mining_speed"),
        4,
        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    );

    public DivingHelmetItem(Properties settings) {
        super(settings);
    }

    public static ItemAttributeModifiers createAttributeModifiers(ArmorMaterial material) {
        return new ItemAttributeModifiers(ImmutableList.<ItemAttributeModifiers.Entry>builder()
            .addAll(material.createAttributes(ArmorType.HELMET).modifiers())
            .add(new ItemAttributeModifiers.Entry(
                Attributes.SUBMERGED_MINING_SPEED,
                DivingHelmetItem.SPEED_MODIFIER,
                EquipmentSlotGroup.HEAD
            )).build());
    }

    public static ItemStack getWornItem(Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = livingEntity.getItemBySlot(SLOT);
        if (!(stack.getItem() instanceof DivingHelmetItem)) {
            return ItemStack.EMPTY;
        }
        return stack;
    }

    public static void breatheInLava(ServerPlayer player, ServerLevel world) {
        ItemStack helmet = getWornItem(player);
        if (helmet.isEmpty())
            return;
        if (helmet.canBeHurtBy(world.damageSources().lava()))
            return;

        List<ItemStack> backtanks = BacktankUtil.getAllWithAir(player);
        if (backtanks.isEmpty())
            return;
        AllAdvancements.DIVING_SUIT_LAVA.trigger(player);
        if (backtanks.stream().allMatch(backtank -> backtank.canBeHurtBy(world.damageSources().lava())))
            return;

        if (world.getGameTime() % 20 == 0)
            BacktankUtil.consumeAir(player, backtanks.getFirst(), 1);
    }

    public static boolean breatheUnderwater(ServerPlayer player, ServerLevel world) {
        ItemStack helmet = getWornItem(player);
        if (helmet.isEmpty())
            return false;

        List<ItemStack> backtanks = BacktankUtil.getAllWithAir(player);
        if (backtanks.isEmpty())
            return false;

        if (world.getGameTime() % 20 == 0)
            BacktankUtil.consumeAir(player, backtanks.getFirst(), 1);

        AllAdvancements.DIVING_SUIT.trigger(player);
        return true;
    }
}

package com.zurrtum.create.content.equipment.armor;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.AllAdvancements;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.List;

import static com.zurrtum.create.Create.MOD_ID;

public class DivingHelmetItem extends Item {
    public static final EquipmentSlot SLOT = EquipmentSlot.HEAD;
    public static final EntityAttributeModifier SPEED_MODIFIER = new EntityAttributeModifier(
        Identifier.of(MOD_ID, "netherite_diving_mining_speed"),
        4,
        EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    );

    public DivingHelmetItem(Settings settings) {
        super(settings);
    }

    public static AttributeModifiersComponent createAttributeModifiers(ArmorMaterial material) {
        return new AttributeModifiersComponent(ImmutableList.<AttributeModifiersComponent.Entry>builder()
            .addAll(material.createAttributeModifiers(EquipmentType.HELMET).modifiers())
            .add(new AttributeModifiersComponent.Entry(
                EntityAttributes.SUBMERGED_MINING_SPEED,
                DivingHelmetItem.SPEED_MODIFIER,
                AttributeModifierSlot.HEAD
            )).build());
    }

    public static ItemStack getWornItem(Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = livingEntity.getEquippedStack(SLOT);
        if (!(stack.getItem() instanceof DivingHelmetItem)) {
            return ItemStack.EMPTY;
        }
        return stack;
    }

    public static void breatheInLava(ServerPlayerEntity player, ServerWorld world) {
        ItemStack helmet = getWornItem(player);
        if (helmet.isEmpty())
            return;
        if (helmet.takesDamageFrom(world.getDamageSources().lava()))
            return;

        List<ItemStack> backtanks = BacktankUtil.getAllWithAir(player);
        if (backtanks.isEmpty())
            return;
        AllAdvancements.DIVING_SUIT_LAVA.trigger(player);
        if (backtanks.stream().allMatch(backtank -> backtank.takesDamageFrom(world.getDamageSources().lava())))
            return;

        if (world.getTime() % 20 == 0)
            BacktankUtil.consumeAir(player, backtanks.getFirst(), 1);
    }

    public static boolean breatheUnderwater(ServerPlayerEntity player, ServerWorld world) {
        ItemStack helmet = getWornItem(player);
        if (helmet.isEmpty())
            return false;

        List<ItemStack> backtanks = BacktankUtil.getAllWithAir(player);
        if (backtanks.isEmpty())
            return false;

        if (world.getTime() % 20 == 0)
            BacktankUtil.consumeAir(player, backtanks.getFirst(), 1);

        AllAdvancements.DIVING_SUIT.trigger(player);
        return true;
    }
}

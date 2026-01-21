package com.zurrtum.create.content.equipment.extendoGrip;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.equipment.armor.BacktankUtil;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.Create.MOD_ID;

public class ExtendoGripItem extends Item {
    public static final AttributeModifier singleRangeAttributeModifier = new AttributeModifier(
        Identifier.fromNamespaceAndPath(
            MOD_ID,
            "single_range_attribute_modifier"
    ), 3, AttributeModifier.Operation.ADD_VALUE
    );
    public static final AttributeModifier doubleRangeAttributeModifier = new AttributeModifier(
        Identifier.fromNamespaceAndPath(
            MOD_ID,
            "double_range_attribute_modifier"
    ), 2, AttributeModifier.Operation.ADD_VALUE
    );
    public static final AttributeModifier attackKnockbackAttributeModifier = new AttributeModifier(
        Identifier.fromNamespaceAndPath(
            MOD_ID,
            "attack_knockback_attribute_modifier"
    ), 4, AttributeModifier.Operation.ADD_VALUE
    );
    public static final ItemAttributeModifiers rangeModifier = ItemAttributeModifiers.builder()
        .add(Attributes.BLOCK_INTERACTION_RANGE, singleRangeAttributeModifier, EquipmentSlotGroup.HAND)
        .add(Attributes.ENTITY_INTERACTION_RANGE, singleRangeAttributeModifier, EquipmentSlotGroup.HAND)
        .add(Attributes.ATTACK_KNOCKBACK, attackKnockbackAttributeModifier, EquipmentSlotGroup.HAND).build();
    public static final ItemAttributeModifiers doubleRangeModifier = ItemAttributeModifiers.builder()
        .add(Attributes.BLOCK_INTERACTION_RANGE, doubleRangeAttributeModifier, EquipmentSlotGroup.MAINHAND)
        .add(Attributes.ENTITY_INTERACTION_RANGE, doubleRangeAttributeModifier, EquipmentSlotGroup.MAINHAND).build();

    public ExtendoGripItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
        ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (slot == EquipmentSlot.MAINHAND) {
            if (entity instanceof LivingEntity livingEntity && livingEntity.getItemBySlot(EquipmentSlot.OFFHAND).is(AllItems.EXTENDO_GRIP)) {
                if (modifiers != doubleRangeModifier) {
                    stack.set(DataComponents.ATTRIBUTE_MODIFIERS, doubleRangeModifier);
                    livingEntity.lastEquipmentItems.get(slot).remove(DataComponents.ATTRIBUTE_MODIFIERS);
                    if (entity instanceof ServerPlayer serverPlayer) {
                        AllAdvancements.EXTENDO_GRIP_DUAL.trigger(serverPlayer);
                    }
                }
            } else {
                applyAttributeModifiers(stack, modifiers);
                if (entity instanceof ServerPlayer serverPlayer) {
                    AllAdvancements.EXTENDO_GRIP.trigger(serverPlayer);
                }
            }
        } else {
            applyAttributeModifiers(stack, modifiers);
        }
    }

    private static void applyAttributeModifiers(ItemStack stack, @Nullable ItemAttributeModifiers oldComponent) {
        if (oldComponent != rangeModifier) {
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, rangeModifier);
        }
    }

    public static void postDamageEntity(Player player) {
        if (damageMainHand(player, player.getMainHandItem())) {
            return;
        }
        damageOffHand(player);
    }

    public static void postPlace(Player player) {
        damageOffHand(player);
    }

    public static void postMine(Player player, ItemStack stack) {
        if (damageMainHand(player, stack)) {
            return;
        }
        damageOffHand(player);
    }

    private static boolean damageMainHand(Player player, ItemStack stack) {
        if (stack.is(AllItems.EXTENDO_GRIP)) {
            damage(player, EquipmentSlot.MAINHAND, stack);
            return true;
        }
        return false;
    }

    private static void damageOffHand(Player player) {
        ItemStack stack = player.getOffhandItem();
        if (stack.is(AllItems.EXTENDO_GRIP)) {
            damage(player, EquipmentSlot.OFFHAND, stack);
        }
    }

    private static void damage(Player player, EquipmentSlot slot, ItemStack stack) {
        if (!BacktankUtil.canAbsorbDamage(player, maxUses())) {
            stack.hurtAndBreak(1, player, slot);
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return BacktankUtil.isBarVisible(stack, maxUses());
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return BacktankUtil.getBarWidth(stack, maxUses());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return BacktankUtil.getBarColor(stack, maxUses());
    }

    private static int maxUses() {
        return AllConfigs.server().equipment.maxExtendoGripActions.get();
    }

    public static boolean shouldInteraction(Player player, InteractionHand hand, ItemStack stack) {
        if (stack.isEmpty()) {
            return player.getItemBySlot(hand == InteractionHand.MAIN_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND).is(AllItems.EXTENDO_GRIP);
        }
        if (stack.is(AllItems.EXTENDO_GRIP)) {
            stack = player.getItemBySlot(hand == InteractionHand.MAIN_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND);
            return stack.isEmpty() || stack.is(AllItems.EXTENDO_GRIP);
        }
        return false;
    }
}

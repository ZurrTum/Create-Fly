package com.zurrtum.create.content.equipment.extendoGrip;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.equipment.armor.BacktankUtil;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import static com.zurrtum.create.Create.MOD_ID;

public class ExtendoGripItem extends Item {
    public static final EntityAttributeModifier singleRangeAttributeModifier = new EntityAttributeModifier(
        Identifier.of(
            MOD_ID,
            "single_range_attribute_modifier"
    ), 3, EntityAttributeModifier.Operation.ADD_VALUE
    );
    public static final EntityAttributeModifier doubleRangeAttributeModifier = new EntityAttributeModifier(
        Identifier.of(
            MOD_ID,
            "double_range_attribute_modifier"
    ), 2, EntityAttributeModifier.Operation.ADD_VALUE
    );
    public static final EntityAttributeModifier attackKnockbackAttributeModifier = new EntityAttributeModifier(
        Identifier.of(
            MOD_ID,
            "attack_knockback_attribute_modifier"
    ), 4, EntityAttributeModifier.Operation.ADD_VALUE
    );
    public static final AttributeModifiersComponent rangeModifier = AttributeModifiersComponent.builder()
        .add(EntityAttributes.BLOCK_INTERACTION_RANGE, singleRangeAttributeModifier, AttributeModifierSlot.HAND)
        .add(EntityAttributes.ENTITY_INTERACTION_RANGE, singleRangeAttributeModifier, AttributeModifierSlot.HAND)
        .add(EntityAttributes.ATTACK_KNOCKBACK, attackKnockbackAttributeModifier, AttributeModifierSlot.HAND).build();
    public static final AttributeModifiersComponent doubleRangeModifier = AttributeModifiersComponent.builder()
        .add(EntityAttributes.BLOCK_INTERACTION_RANGE, doubleRangeAttributeModifier, AttributeModifierSlot.MAINHAND)
        .add(EntityAttributes.ENTITY_INTERACTION_RANGE, doubleRangeAttributeModifier, AttributeModifierSlot.MAINHAND).build();

    public ExtendoGripItem(Settings properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot) {
        AttributeModifiersComponent modifiers = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (slot == EquipmentSlot.MAINHAND) {
            if (entity instanceof LivingEntity livingEntity && livingEntity.getEquippedStack(EquipmentSlot.OFFHAND).isOf(AllItems.EXTENDO_GRIP)) {
                if (modifiers != doubleRangeModifier) {
                    stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, doubleRangeModifier);
                    livingEntity.lastEquipmentStacks.get(slot).remove(DataComponentTypes.ATTRIBUTE_MODIFIERS);
                    if (entity instanceof ServerPlayerEntity serverPlayer) {
                        AllAdvancements.EXTENDO_GRIP_DUAL.trigger(serverPlayer);
                    }
                }
            } else {
                applyAttributeModifiers(stack, modifiers, rangeModifier);
                if (entity instanceof ServerPlayerEntity serverPlayer) {
                    AllAdvancements.EXTENDO_GRIP.trigger(serverPlayer);
                }
            }
        } else {
            applyAttributeModifiers(stack, modifiers, rangeModifier);
        }
    }

    private static void applyAttributeModifiers(ItemStack stack, AttributeModifiersComponent oldComponent, AttributeModifiersComponent newComponent) {
        if (oldComponent != newComponent) {
            stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, newComponent);
        }
    }

    public static void postDamageEntity(PlayerEntity player) {
        if (damageMainHand(player, player.getMainHandStack())) {
            return;
        }
        damageOffHand(player);
    }

    public static void postPlace(PlayerEntity player) {
        damageOffHand(player);
    }

    public static void postMine(PlayerEntity player, ItemStack stack) {
        if (damageMainHand(player, stack)) {
            return;
        }
        damageOffHand(player);
    }

    private static boolean damageMainHand(PlayerEntity player, ItemStack stack) {
        if (stack.isOf(AllItems.EXTENDO_GRIP)) {
            damage(player, EquipmentSlot.MAINHAND, stack);
            return true;
        }
        return false;
    }

    private static void damageOffHand(PlayerEntity player) {
        ItemStack stack = player.getOffHandStack();
        if (stack.isOf(AllItems.EXTENDO_GRIP)) {
            damage(player, EquipmentSlot.OFFHAND, stack);
        }
    }

    private static void damage(PlayerEntity player, EquipmentSlot slot, ItemStack stack) {
        if (!BacktankUtil.canAbsorbDamage(player, maxUses())) {
            stack.damage(1, player, slot);
        }
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return BacktankUtil.isBarVisible(stack, maxUses());
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return BacktankUtil.getBarWidth(stack, maxUses());
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return BacktankUtil.getBarColor(stack, maxUses());
    }

    private static int maxUses() {
        return AllConfigs.server().equipment.maxExtendoGripActions.get();
    }

    public static boolean shouldInteraction(PlayerEntity player, Hand hand, ItemStack stack) {
        if (stack.isOf(AllItems.EXTENDO_GRIP)) {
            return true;
        }
        return player.getEquippedStack(hand == Hand.MAIN_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND).isOf(AllItems.EXTENDO_GRIP);
    }
}

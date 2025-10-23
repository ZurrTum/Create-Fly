package com.zurrtum.create.content.equipment.goggles;

import com.zurrtum.create.AllItems;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class GogglesItem extends Item {
    private static final List<Predicate<PlayerEntity>> IS_WEARING_PREDICATES = new ArrayList<>();

    static {
        addIsWearingPredicate(player -> player.getEquippedStack(EquipmentSlot.HEAD).isOf(AllItems.GOGGLES));
    }

    public GogglesItem(Item.Settings properties) {
        super(properties);
    }

    public static boolean isWearingGoggles(PlayerEntity player) {
        for (Predicate<PlayerEntity> predicate : IS_WEARING_PREDICATES) {
            if (predicate.test(player)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Use this method to add custom entry points to the goggles overlay, e.g. custom
     * armor, handheld alternatives, etc.
     */
    public static synchronized void addIsWearingPredicate(Predicate<PlayerEntity> predicate) {
        IS_WEARING_PREDICATES.add(predicate);
    }
}
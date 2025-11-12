package com.zurrtum.create.content.equipment.goggles;

import com.zurrtum.create.AllItems;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class GogglesItem extends Item {
    private static final List<Predicate<Player>> IS_WEARING_PREDICATES = new ArrayList<>();

    static {
        addIsWearingPredicate(player -> player.getItemBySlot(EquipmentSlot.HEAD).is(AllItems.GOGGLES));
    }

    public GogglesItem(Item.Properties properties) {
        super(properties);
    }

    public static boolean isWearingGoggles(Player player) {
        for (Predicate<Player> predicate : IS_WEARING_PREDICATES) {
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
    public static synchronized void addIsWearingPredicate(Predicate<Player> predicate) {
        IS_WEARING_PREDICATES.add(predicate);
    }
}
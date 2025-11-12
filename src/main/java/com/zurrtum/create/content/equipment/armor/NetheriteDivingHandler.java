package com.zurrtum.create.content.equipment.armor;

import com.zurrtum.create.AllSynchedDatas;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class NetheriteDivingHandler {
    public static void onEquipmentChange(Player player) {
        if (AllSynchedDatas.FIRE_IMMUNE.get(player)) {
            if (isValidArmorSet(player)) {
                return;
            }
            AllSynchedDatas.FIRE_IMMUNE.set(player, false);
        } else if (isValidArmorSet(player)) {
            AllSynchedDatas.FIRE_IMMUNE.set(player, true);
        }

    }

    private static boolean isValidArmorSet(Player player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!(head.getItem() instanceof DivingHelmetItem) || head.canBeHurtBy(player.level().damageSources().lava())) {
            return false;
        }

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof BacktankItem) || chest.canBeHurtBy(player.level().damageSources().lava()) || !BacktankUtil.hasAirRemaining(
            chest)) {
            return false;
        }

        if (player.getItemBySlot(EquipmentSlot.LEGS).canBeHurtBy(player.level().damageSources().lava())) {
            return false;
        }

        return !player.getItemBySlot(EquipmentSlot.FEET).canBeHurtBy(player.level().damageSources().lava());
    }
}

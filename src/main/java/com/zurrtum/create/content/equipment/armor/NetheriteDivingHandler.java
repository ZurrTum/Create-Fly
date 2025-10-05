package com.zurrtum.create.content.equipment.armor;

import com.zurrtum.create.AllSynchedDatas;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class NetheriteDivingHandler {
    public static void onEquipmentChange(PlayerEntity player) {
        if (AllSynchedDatas.FIRE_IMMUNE.get(player)) {
            if (isValidArmorSet(player)) {
                return;
            }
            AllSynchedDatas.FIRE_IMMUNE.set(player, false);
        } else if (isValidArmorSet(player)) {
            AllSynchedDatas.FIRE_IMMUNE.set(player, true);
        }

    }

    private static boolean isValidArmorSet(PlayerEntity player) {
        ItemStack head = player.getEquippedStack(EquipmentSlot.HEAD);
        if (!(head.getItem() instanceof DivingHelmetItem) || head.takesDamageFrom(player.getEntityWorld().getDamageSources().lava())) {
            return false;
        }

        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof BacktankItem) || chest.takesDamageFrom(player.getEntityWorld().getDamageSources()
            .lava()) || !BacktankUtil.hasAirRemaining(chest)) {
            return false;
        }

        if (player.getEquippedStack(EquipmentSlot.LEGS).takesDamageFrom(player.getEntityWorld().getDamageSources().lava())) {
            return false;
        }

        return !player.getEquippedStack(EquipmentSlot.FEET).takesDamageFrom(player.getEntityWorld().getDamageSources().lava());
    }
}

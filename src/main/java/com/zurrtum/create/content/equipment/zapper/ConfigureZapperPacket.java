package com.zurrtum.create.content.equipment.zapper;

import com.zurrtum.create.infrastructure.component.PlacementPatterns;
import com.zurrtum.create.infrastructure.packet.c2s.C2SPacket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public interface ConfigureZapperPacket extends C2SPacket {
    Hand hand();

    PlacementPatterns pattern();

    void configureZapper(ItemStack stack);
}

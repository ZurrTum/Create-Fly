package com.zurrtum.create.content.equipment.zapper;

import com.zurrtum.create.infrastructure.component.PlacementPatterns;
import com.zurrtum.create.infrastructure.packet.c2s.C2SPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public interface ConfigureZapperPacket extends C2SPacket {
    InteractionHand hand();

    PlacementPatterns pattern();

    void configureZapper(ItemStack stack);
}

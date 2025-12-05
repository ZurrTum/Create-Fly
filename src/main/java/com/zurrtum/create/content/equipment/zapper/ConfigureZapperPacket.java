package com.zurrtum.create.content.equipment.zapper;

import com.zurrtum.create.infrastructure.component.PlacementPatterns;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Hand;

public interface ConfigureZapperPacket extends Packet<ServerPlayPacketListener> {
    Hand hand();

    PlacementPatterns pattern();

    void configureZapper(ItemStack stack);
}

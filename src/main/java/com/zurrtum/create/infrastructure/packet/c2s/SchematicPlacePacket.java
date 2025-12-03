package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public record SchematicPlacePacket(ItemStack stack) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, SchematicPlacePacket> CODEC = ItemStack.PACKET_CODEC.xmap(
        SchematicPlacePacket::new,
        SchematicPlacePacket::stack
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onSchematicPlace((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<SchematicPlacePacket> getPacketType() {
        return AllPackets.PLACE_SCHEMATIC;
    }
}

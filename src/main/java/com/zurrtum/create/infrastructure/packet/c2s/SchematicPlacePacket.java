package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;

public record SchematicPlacePacket(ItemStack stack) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, SchematicPlacePacket> CODEC = ItemStack.STREAM_CODEC.map(
        SchematicPlacePacket::new,
        SchematicPlacePacket::stack
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onSchematicPlace((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<SchematicPlacePacket> type() {
        return AllPackets.PLACE_SCHEMATIC;
    }
}

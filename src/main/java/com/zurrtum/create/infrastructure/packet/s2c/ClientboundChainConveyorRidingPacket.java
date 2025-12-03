package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.Uuids;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public record ClientboundChainConveyorRidingPacket(Collection<UUID> uuids) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<ByteBuf, ClientboundChainConveyorRidingPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.collection(HashSet::new, Uuids.PACKET_CODEC),
        ClientboundChainConveyorRidingPacket::uuids,
        ClientboundChainConveyorRidingPacket::new
    );

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onClientboundChainConveyorRiding(this);
    }

    @Override
    public PacketType<ClientboundChainConveyorRidingPacket> getPacketType() {
        return AllPackets.CLIENTBOUND_CHAIN_CONVEYOR;
    }
}

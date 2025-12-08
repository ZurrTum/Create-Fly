package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public record ClientboundChainConveyorRidingPacket(Collection<UUID> uuids) implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<ByteBuf, ClientboundChainConveyorRidingPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.collection(HashSet::new, UUIDUtil.STREAM_CODEC),
        ClientboundChainConveyorRidingPacket::uuids,
        ClientboundChainConveyorRidingPacket::new
    );

    @Override
    public void handle(ClientGamePacketListener listener) {
        AllClientHandle.INSTANCE.onClientboundChainConveyorRiding(this);
    }

    @Override
    public PacketType<ClientboundChainConveyorRidingPacket> type() {
        return AllPackets.CLIENTBOUND_CHAIN_CONVEYOR;
    }
}

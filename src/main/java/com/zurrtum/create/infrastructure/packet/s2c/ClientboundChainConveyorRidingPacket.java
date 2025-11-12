package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;

public record ClientboundChainConveyorRidingPacket(Collection<UUID> uuids) implements S2CPacket {
    public static final StreamCodec<ByteBuf, ClientboundChainConveyorRidingPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.collection(HashSet::new, UUIDUtil.STREAM_CODEC),
        ClientboundChainConveyorRidingPacket::uuids,
        ClientboundChainConveyorRidingPacket::new
    );

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, ClientboundChainConveyorRidingPacket> callback() {
        return AllClientHandle::onClientboundChainConveyorRiding;
    }

    @Override
    public PacketType<ClientboundChainConveyorRidingPacket> type() {
        return AllPackets.CLIENTBOUND_CHAIN_CONVEYOR;
    }
}

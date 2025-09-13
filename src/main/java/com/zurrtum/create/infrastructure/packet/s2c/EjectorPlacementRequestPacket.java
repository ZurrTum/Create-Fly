package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.util.TriConsumer;

public record EjectorPlacementRequestPacket(BlockPos pos) implements S2CPacket {
    public static final PacketCodec<ByteBuf, EjectorPlacementRequestPacket> CODEC = BlockPos.PACKET_CODEC.xmap(
        EjectorPlacementRequestPacket::new,
        EjectorPlacementRequestPacket::pos
    );

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, EjectorPlacementRequestPacket> callback() {
        return AllClientHandle::onEjectorPlacementRequest;
    }

    @Override
    public PacketType<EjectorPlacementRequestPacket> getPacketType() {
        return AllPackets.S_PLACE_EJECTOR;
    }
}

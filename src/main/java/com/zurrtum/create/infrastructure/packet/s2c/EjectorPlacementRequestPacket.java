package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import org.apache.logging.log4j.util.TriConsumer;

public record EjectorPlacementRequestPacket(BlockPos pos) implements S2CPacket {
    public static final StreamCodec<ByteBuf, EjectorPlacementRequestPacket> CODEC = BlockPos.STREAM_CODEC.map(
        EjectorPlacementRequestPacket::new,
        EjectorPlacementRequestPacket::pos
    );

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, EjectorPlacementRequestPacket> callback() {
        return AllClientHandle::onEjectorPlacementRequest;
    }

    @Override
    public PacketType<EjectorPlacementRequestPacket> type() {
        return AllPackets.S_PLACE_EJECTOR;
    }
}

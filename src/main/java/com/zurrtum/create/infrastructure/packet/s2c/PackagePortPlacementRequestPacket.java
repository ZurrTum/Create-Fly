package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import org.apache.logging.log4j.util.TriConsumer;

public record PackagePortPlacementRequestPacket(BlockPos pos) implements S2CPacket {
    public static final StreamCodec<ByteBuf, PackagePortPlacementRequestPacket> CODEC = BlockPos.STREAM_CODEC.map(
        PackagePortPlacementRequestPacket::new,
        PackagePortPlacementRequestPacket::pos
    );

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, PackagePortPlacementRequestPacket> callback() {
        return AllClientHandle::onPackagePortPlacementRequest;
    }

    @Override
    public PacketType<PackagePortPlacementRequestPacket> type() {
        return AllPackets.S_PLACE_PACKAGE_PORT;
    }
}

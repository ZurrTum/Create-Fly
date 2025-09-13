package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.util.TriConsumer;

public record PackagePortPlacementRequestPacket(BlockPos pos) implements S2CPacket {
    public static final PacketCodec<ByteBuf, PackagePortPlacementRequestPacket> CODEC = BlockPos.PACKET_CODEC.xmap(
        PackagePortPlacementRequestPacket::new,
        PackagePortPlacementRequestPacket::pos
    );

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, PackagePortPlacementRequestPacket> callback() {
        return AllClientHandle::onPackagePortPlacementRequest;
    }

    @Override
    public PacketType<PackagePortPlacementRequestPacket> getPacketType() {
        return AllPackets.S_PLACE_PACKAGE_PORT;
    }
}

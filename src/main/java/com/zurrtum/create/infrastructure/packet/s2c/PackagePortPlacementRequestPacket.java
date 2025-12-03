package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.BlockPos;

public record PackagePortPlacementRequestPacket(BlockPos pos) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<ByteBuf, PackagePortPlacementRequestPacket> CODEC = BlockPos.PACKET_CODEC.xmap(
        PackagePortPlacementRequestPacket::new,
        PackagePortPlacementRequestPacket::pos
    );

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onPackagePortPlacementRequest(this);
    }

    @Override
    public PacketType<PackagePortPlacementRequestPacket> getPacketType() {
        return AllPackets.S_PLACE_PACKAGE_PORT;
    }
}

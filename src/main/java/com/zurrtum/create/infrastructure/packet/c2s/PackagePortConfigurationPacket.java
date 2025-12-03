package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

public record PackagePortConfigurationPacket(BlockPos pos, String newFilter, boolean acceptPackages) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<ByteBuf, PackagePortConfigurationPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        PackagePortConfigurationPacket::pos,
        PacketCodecs.STRING,
        PackagePortConfigurationPacket::newFilter,
        PacketCodecs.BOOLEAN,
        PackagePortConfigurationPacket::acceptPackages,
        PackagePortConfigurationPacket::new
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onPackagePortConfiguration((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<PackagePortConfigurationPacket> getPacketType() {
        return AllPackets.PACKAGE_PORT_CONFIGURATION;
    }
}

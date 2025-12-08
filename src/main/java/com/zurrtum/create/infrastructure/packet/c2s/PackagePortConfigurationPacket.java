package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record PackagePortConfigurationPacket(BlockPos pos, String newFilter, boolean acceptPackages) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<ByteBuf, PackagePortConfigurationPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        PackagePortConfigurationPacket::pos,
        ByteBufCodecs.STRING_UTF8,
        PackagePortConfigurationPacket::newFilter,
        ByteBufCodecs.BOOL,
        PackagePortConfigurationPacket::acceptPackages,
        PackagePortConfigurationPacket::new
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onPackagePortConfiguration((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<PackagePortConfigurationPacket> type() {
        return AllPackets.PACKAGE_PORT_CONFIGURATION;
    }
}

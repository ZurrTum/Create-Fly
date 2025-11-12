package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;

import java.util.function.BiConsumer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record PackagePortConfigurationPacket(BlockPos pos, String newFilter, boolean acceptPackages) implements C2SPacket {
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
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<PackagePortConfigurationPacket> type() {
        return AllPackets.PACKAGE_PORT_CONFIGURATION;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, PackagePortConfigurationPacket> callback() {
        return AllHandle::onPackagePortConfiguration;
    }
}

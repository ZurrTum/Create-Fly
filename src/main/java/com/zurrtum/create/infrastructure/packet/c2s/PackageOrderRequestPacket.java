package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;

public record PackageOrderRequestPacket(BlockPos pos, PackageOrderWithCrafts order, String address, boolean encodeRequester) implements C2SPacket {
    public static final PacketCodec<RegistryByteBuf, PackageOrderRequestPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        PackageOrderRequestPacket::pos,
        PackageOrderWithCrafts.STREAM_CODEC,
        PackageOrderRequestPacket::order,
        PacketCodecs.STRING,
        PackageOrderRequestPacket::address,
        PacketCodecs.BOOLEAN,
        PackageOrderRequestPacket::encodeRequester,
        PackageOrderRequestPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<PackageOrderRequestPacket> getPacketType() {
        return AllPackets.LOGISTICS_PACKAGE_REQUEST;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, PackageOrderRequestPacket> callback() {
        return AllHandle::onPackageOrderRequest;
    }
}

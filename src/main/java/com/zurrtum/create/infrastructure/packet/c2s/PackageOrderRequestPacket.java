package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.infrastructure.component.PackageOrderWithCrafts;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record PackageOrderRequestPacket(BlockPos pos, PackageOrderWithCrafts order, String address, boolean encodeRequester) implements C2SPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, PackageOrderRequestPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        PackageOrderRequestPacket::pos,
        PackageOrderWithCrafts.STREAM_CODEC,
        PackageOrderRequestPacket::order,
        ByteBufCodecs.STRING_UTF8,
        PackageOrderRequestPacket::address,
        ByteBufCodecs.BOOL,
        PackageOrderRequestPacket::encodeRequester,
        PackageOrderRequestPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<PackageOrderRequestPacket> type() {
        return AllPackets.LOGISTICS_PACKAGE_REQUEST;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, PackageOrderRequestPacket> callback() {
        return AllHandle::onPackageOrderRequest;
    }
}

package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public record RedstoneRequesterConfigurationPacket(
    BlockPos pos, String address, boolean allowPartial, List<Integer> amounts
) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<ByteBuf, RedstoneRequesterConfigurationPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        RedstoneRequesterConfigurationPacket::pos,
        PacketCodecs.STRING,
        RedstoneRequesterConfigurationPacket::address,
        PacketCodecs.BOOLEAN,
        RedstoneRequesterConfigurationPacket::allowPartial,
        CatnipStreamCodecBuilders.list(PacketCodecs.INTEGER),
        RedstoneRequesterConfigurationPacket::amounts,
        RedstoneRequesterConfigurationPacket::new
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onRedstoneRequesterConfiguration((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<RedstoneRequesterConfigurationPacket> getPacketType() {
        return AllPackets.CONFIGURE_REDSTONE_REQUESTER;
    }
}

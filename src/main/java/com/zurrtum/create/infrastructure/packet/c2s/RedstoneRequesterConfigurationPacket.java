package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.List;

public record RedstoneRequesterConfigurationPacket(
    BlockPos pos, String address, boolean allowPartial, List<Integer> amounts
) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<ByteBuf, RedstoneRequesterConfigurationPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        RedstoneRequesterConfigurationPacket::pos,
        ByteBufCodecs.STRING_UTF8,
        RedstoneRequesterConfigurationPacket::address,
        ByteBufCodecs.BOOL,
        RedstoneRequesterConfigurationPacket::allowPartial,
        CatnipStreamCodecBuilders.list(ByteBufCodecs.INT),
        RedstoneRequesterConfigurationPacket::amounts,
        RedstoneRequesterConfigurationPacket::new
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onRedstoneRequesterConfiguration((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<RedstoneRequesterConfigurationPacket> type() {
        return AllPackets.CONFIGURE_REDSTONE_REQUESTER;
    }
}

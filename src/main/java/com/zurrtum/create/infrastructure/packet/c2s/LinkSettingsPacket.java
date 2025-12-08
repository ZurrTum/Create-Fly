package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;

public record LinkSettingsPacket(BlockPos pos, boolean first, InteractionHand hand) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<ByteBuf, LinkSettingsPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        LinkSettingsPacket::pos,
        ByteBufCodecs.BOOL,
        LinkSettingsPacket::first,
        CatnipStreamCodecs.HAND,
        LinkSettingsPacket::hand,
        LinkSettingsPacket::new
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onLinkSettings((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<LinkSettingsPacket> type() {
        return AllPackets.LINK_SETTINGS;
    }
}

package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import io.netty.buffer.ByteBuf;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;

public record LinkSettingsPacket(BlockPos pos, boolean first, InteractionHand hand) implements C2SPacket {
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
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<LinkSettingsPacket> type() {
        return AllPackets.LINK_SETTINGS;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, LinkSettingsPacket> callback() {
        return AllHandle::onLinkSettings;
    }
}

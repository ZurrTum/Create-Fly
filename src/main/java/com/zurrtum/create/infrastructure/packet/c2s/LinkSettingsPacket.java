package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;

public record LinkSettingsPacket(BlockPos pos, boolean first, Hand hand) implements C2SPacket {
    public static final PacketCodec<ByteBuf, LinkSettingsPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        LinkSettingsPacket::pos,
        PacketCodecs.BOOLEAN,
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
    public PacketType<LinkSettingsPacket> getPacketType() {
        return AllPackets.LINK_SETTINGS;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, LinkSettingsPacket> callback() {
        return AllHandle::onLinkSettings;
    }
}

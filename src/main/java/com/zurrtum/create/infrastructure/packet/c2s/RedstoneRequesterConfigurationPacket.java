package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.function.BiConsumer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record RedstoneRequesterConfigurationPacket(BlockPos pos, String address, boolean allowPartial, List<Integer> amounts) implements C2SPacket {
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
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<RedstoneRequesterConfigurationPacket> type() {
        return AllPackets.CONFIGURE_REDSTONE_REQUESTER;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, RedstoneRequesterConfigurationPacket> callback() {
        return AllHandle::onRedstoneRequesterConfiguration;
    }
}

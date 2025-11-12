package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record DisplayLinkConfigurationPacket(BlockPos pos, CompoundTag configData, int targetLine) implements C2SPacket {
    public static final StreamCodec<ByteBuf, DisplayLinkConfigurationPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        DisplayLinkConfigurationPacket::pos,
        ByteBufCodecs.COMPOUND_TAG,
        DisplayLinkConfigurationPacket::configData,
        ByteBufCodecs.VAR_INT,
        DisplayLinkConfigurationPacket::targetLine,
        DisplayLinkConfigurationPacket::new
    );

    @Override
    public PacketType<DisplayLinkConfigurationPacket> type() {
        return AllPackets.CONFIGURE_DATA_GATHERER;
    }

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, DisplayLinkConfigurationPacket> callback() {
        return AllHandle::onDisplayLinkConfiguration;
    }
}

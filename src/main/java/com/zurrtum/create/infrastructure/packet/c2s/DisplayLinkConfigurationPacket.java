package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;

public record DisplayLinkConfigurationPacket(BlockPos pos, NbtCompound configData, int targetLine) implements C2SPacket {
    public static final PacketCodec<ByteBuf, DisplayLinkConfigurationPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        DisplayLinkConfigurationPacket::pos,
        PacketCodecs.NBT_COMPOUND,
        DisplayLinkConfigurationPacket::configData,
        PacketCodecs.VAR_INT,
        DisplayLinkConfigurationPacket::targetLine,
        DisplayLinkConfigurationPacket::new
    );

    @Override
    public PacketType<DisplayLinkConfigurationPacket> getPacketType() {
        return AllPackets.CONFIGURE_DATA_GATHERER;
    }

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, DisplayLinkConfigurationPacket> callback() {
        return AllHandle::onDisplayLinkConfiguration;
    }
}

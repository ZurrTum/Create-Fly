package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

public record DisplayLinkConfigurationPacket(BlockPos pos, NbtCompound configData, int targetLine) implements Packet<ServerPlayPacketListener> {
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
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onDisplayLinkConfiguration((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<DisplayLinkConfigurationPacket> getPacketType() {
        return AllPackets.CONFIGURE_DATA_GATHERER;
    }
}

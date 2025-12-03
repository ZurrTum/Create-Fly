package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.kinetics.transmission.sequencer.Instruction;
import com.zurrtum.create.foundation.codec.CreateStreamCodecs;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

import java.util.Vector;

public record ConfigureSequencedGearshiftPacket(BlockPos pos, Vector<Instruction> instructions) implements Packet<ServerPlayPacketListener> {
    @SuppressWarnings("removal")
    public static final PacketCodec<RegistryByteBuf, ConfigureSequencedGearshiftPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        ConfigureSequencedGearshiftPacket::pos,
        Instruction.STREAM_CODEC.collect(CreateStreamCodecs.vector()),
        ConfigureSequencedGearshiftPacket::instructions,
        ConfigureSequencedGearshiftPacket::new
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onConfigureSequencedGearshift((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<ConfigureSequencedGearshiftPacket> getPacketType() {
        return AllPackets.CONFIGURE_SEQUENCER;
    }
}

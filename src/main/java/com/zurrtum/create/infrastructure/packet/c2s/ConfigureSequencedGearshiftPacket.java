package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.kinetics.transmission.sequencer.Instruction;
import com.zurrtum.create.foundation.codec.CreateStreamCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.Vector;

public record ConfigureSequencedGearshiftPacket(BlockPos pos, Vector<Instruction> instructions) implements Packet<ServerGamePacketListener> {
    @SuppressWarnings("removal")
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigureSequencedGearshiftPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        ConfigureSequencedGearshiftPacket::pos,
        Instruction.STREAM_CODEC.apply(CreateStreamCodecs.vector()),
        ConfigureSequencedGearshiftPacket::instructions,
        ConfigureSequencedGearshiftPacket::new
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onConfigureSequencedGearshift((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<ConfigureSequencedGearshiftPacket> type() {
        return AllPackets.CONFIGURE_SEQUENCER;
    }
}

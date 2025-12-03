package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.BlockPos;

public record AttachedComputerPacket(BlockPos pos, boolean hasAttachedComputer) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<ByteBuf, AttachedComputerPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        AttachedComputerPacket::pos,
        PacketCodecs.BOOLEAN,
        AttachedComputerPacket::hasAttachedComputer,
        AttachedComputerPacket::new
    );

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onAttachedComputer(this);
    }

    @Override
    public PacketType<AttachedComputerPacket> getPacketType() {
        return AllPackets.ATTACHED_COMPUTER;
    }
}

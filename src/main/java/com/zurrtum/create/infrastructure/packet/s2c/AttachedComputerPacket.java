package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.util.TriConsumer;

public record AttachedComputerPacket(BlockPos pos, boolean hasAttachedComputer) implements S2CPacket {
    public static final PacketCodec<ByteBuf, AttachedComputerPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        AttachedComputerPacket::pos,
        PacketCodecs.BOOLEAN,
        AttachedComputerPacket::hasAttachedComputer,
        AttachedComputerPacket::new
    );

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, AttachedComputerPacket> callback() {
        return AllClientHandle::onAttachedComputer;
    }

    @Override
    public PacketType<AttachedComputerPacket> getPacketType() {
        return AllPackets.ATTACHED_COMPUTER;
    }
}

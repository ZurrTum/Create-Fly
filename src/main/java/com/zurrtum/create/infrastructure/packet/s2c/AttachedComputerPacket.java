package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public record AttachedComputerPacket(BlockPos pos, boolean hasAttachedComputer) implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<ByteBuf, AttachedComputerPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        AttachedComputerPacket::pos,
        ByteBufCodecs.BOOL,
        AttachedComputerPacket::hasAttachedComputer,
        AttachedComputerPacket::new
    );

    @Override
    public void handle(ClientGamePacketListener listener) {
        AllClientHandle.INSTANCE.onAttachedComputer(this);
    }

    @Override
    public PacketType<AttachedComputerPacket> type() {
        return AllPackets.ATTACHED_COMPUTER;
    }
}

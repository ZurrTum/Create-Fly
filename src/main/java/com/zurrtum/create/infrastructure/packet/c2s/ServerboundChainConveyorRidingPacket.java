package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record ServerboundChainConveyorRidingPacket(BlockPos pos, boolean stop) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<ByteBuf, ServerboundChainConveyorRidingPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        ServerboundChainConveyorRidingPacket::pos,
        ByteBufCodecs.BOOL,
        ServerboundChainConveyorRidingPacket::stop,
        ServerboundChainConveyorRidingPacket::new
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onServerboundChainConveyorRiding((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<ServerboundChainConveyorRidingPacket> type() {
        return AllPackets.CHAIN_CONVEYOR_RIDING;
    }
}

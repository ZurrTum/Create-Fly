package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.decoration.slidingDoor.DoorControl;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record ElevatorContactEditPacket(
    BlockPos pos, String shortName, String longName, DoorControl doorControl
) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<ByteBuf, ElevatorContactEditPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        ElevatorContactEditPacket::pos,
        ByteBufCodecs.stringUtf8(4),
        ElevatorContactEditPacket::shortName,
        ByteBufCodecs.stringUtf8(90),
        ElevatorContactEditPacket::longName,
        DoorControl.STREAM_CODEC,
        ElevatorContactEditPacket::doorControl,
        ElevatorContactEditPacket::new
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onElevatorContactEdit((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<ElevatorContactEditPacket> type() {
        return AllPackets.CONFIGURE_ELEVATOR_CONTACT;
    }
}

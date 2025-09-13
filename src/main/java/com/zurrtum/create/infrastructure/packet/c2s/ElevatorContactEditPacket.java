package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.decoration.slidingDoor.DoorControl;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;

public record ElevatorContactEditPacket(BlockPos pos, String shortName, String longName, DoorControl doorControl) implements C2SPacket {
    public static final PacketCodec<ByteBuf, ElevatorContactEditPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        ElevatorContactEditPacket::pos,
        PacketCodecs.string(4),
        ElevatorContactEditPacket::shortName,
        PacketCodecs.string(90),
        ElevatorContactEditPacket::longName,
        DoorControl.STREAM_CODEC,
        ElevatorContactEditPacket::doorControl,
        ElevatorContactEditPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<ElevatorContactEditPacket> getPacketType() {
        return AllPackets.CONFIGURE_ELEVATOR_CONTACT;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, ElevatorContactEditPacket> callback() {
        return AllHandle::onElevatorContactEdit;
    }
}

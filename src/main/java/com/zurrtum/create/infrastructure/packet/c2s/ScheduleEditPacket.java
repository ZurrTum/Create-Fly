package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.trains.schedule.Schedule;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record ScheduleEditPacket(Schedule schedule) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ScheduleEditPacket> CODEC = Schedule.STREAM_CODEC.map(
        ScheduleEditPacket::new,
        ScheduleEditPacket::schedule
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onScheduleEdit((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<ScheduleEditPacket> type() {
        return AllPackets.CONFIGURE_SCHEDULE;
    }
}

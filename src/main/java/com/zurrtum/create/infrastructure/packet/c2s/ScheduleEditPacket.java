package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.trains.schedule.Schedule;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public record ScheduleEditPacket(Schedule schedule) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, ScheduleEditPacket> CODEC = Schedule.STREAM_CODEC.xmap(
        ScheduleEditPacket::new,
        ScheduleEditPacket::schedule
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onScheduleEdit((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<ScheduleEditPacket> getPacketType() {
        return AllPackets.CONFIGURE_SCHEDULE;
    }
}

package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.trains.schedule.Schedule;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import java.util.function.BiConsumer;

public record ScheduleEditPacket(Schedule schedule) implements C2SPacket {
    public static final PacketCodec<RegistryByteBuf, ScheduleEditPacket> CODEC = Schedule.STREAM_CODEC.xmap(
        ScheduleEditPacket::new,
        ScheduleEditPacket::schedule
    );

    @Override
    public PacketType<ScheduleEditPacket> getPacketType() {
        return AllPackets.CONFIGURE_SCHEDULE;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, ScheduleEditPacket> callback() {
        return AllHandle::onScheduleEdit;
    }
}

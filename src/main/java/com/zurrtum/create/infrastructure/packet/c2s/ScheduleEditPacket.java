package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.trains.schedule.Schedule;
import java.util.function.BiConsumer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record ScheduleEditPacket(Schedule schedule) implements C2SPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ScheduleEditPacket> CODEC = Schedule.STREAM_CODEC.map(
        ScheduleEditPacket::new,
        ScheduleEditPacket::schedule
    );

    @Override
    public PacketType<ScheduleEditPacket> type() {
        return AllPackets.CONFIGURE_SCHEDULE;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, ScheduleEditPacket> callback() {
        return AllHandle::onScheduleEdit;
    }
}

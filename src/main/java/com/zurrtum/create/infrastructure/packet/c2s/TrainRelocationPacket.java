package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;
import java.util.function.BiConsumer;

public record TrainRelocationPacket(
    UUID trainId, BlockPos pos, Vec3d lookAngle, int entityId, boolean direction, BezierTrackPointLocation hoveredBezier
) implements C2SPacket {
    public static final PacketCodec<RegistryByteBuf, TrainRelocationPacket> CODEC = PacketCodec.tuple(
        Uuids.PACKET_CODEC,
        TrainRelocationPacket::trainId,
        BlockPos.PACKET_CODEC,
        TrainRelocationPacket::pos,
        Vec3d.PACKET_CODEC,
        TrainRelocationPacket::lookAngle,
        PacketCodecs.INTEGER,
        TrainRelocationPacket::entityId,
        PacketCodecs.BOOLEAN,
        TrainRelocationPacket::direction,
        CatnipStreamCodecBuilders.nullable(BezierTrackPointLocation.STREAM_CODEC),
        TrainRelocationPacket::hoveredBezier,
        TrainRelocationPacket::new
    );

    @Override
    public PacketType<TrainRelocationPacket> getPacketType() {
        return AllPackets.RELOCATE_TRAIN;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, TrainRelocationPacket> callback() {
        return AllHandle::onTrainRelocation;
    }
}

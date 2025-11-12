package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;

public record TrainRelocationPacket(
    UUID trainId, BlockPos pos, Vec3 lookAngle, int entityId, boolean direction, BezierTrackPointLocation hoveredBezier
) implements C2SPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, TrainRelocationPacket> CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
        TrainRelocationPacket::trainId,
        BlockPos.STREAM_CODEC,
        TrainRelocationPacket::pos,
        Vec3.STREAM_CODEC,
        TrainRelocationPacket::lookAngle,
        ByteBufCodecs.INT,
        TrainRelocationPacket::entityId,
        ByteBufCodecs.BOOL,
        TrainRelocationPacket::direction,
        CatnipStreamCodecBuilders.nullable(BezierTrackPointLocation.STREAM_CODEC),
        TrainRelocationPacket::hoveredBezier,
        TrainRelocationPacket::new
    );

    @Override
    public PacketType<TrainRelocationPacket> type() {
        return AllPackets.RELOCATE_TRAIN;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, TrainRelocationPacket> callback() {
        return AllHandle::onTrainRelocation;
    }
}

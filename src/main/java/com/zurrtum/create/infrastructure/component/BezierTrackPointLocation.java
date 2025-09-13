package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.BlockPos;

public record BezierTrackPointLocation(BlockPos curveTarget, int segment) {
    public static final Codec<BezierTrackPointLocation> CODEC = RecordCodecBuilder.create(i -> i.group(
        BlockPos.CODEC.fieldOf("curve_target").forGetter(BezierTrackPointLocation::curveTarget),
        Codec.INT.fieldOf("segment").forGetter(BezierTrackPointLocation::segment)
    ).apply(i, BezierTrackPointLocation::new));

    public static final PacketCodec<PacketByteBuf, BezierTrackPointLocation> STREAM_CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        BezierTrackPointLocation::curveTarget,
        PacketCodecs.INTEGER,
        BezierTrackPointLocation::segment,
        BezierTrackPointLocation::new
    );
}

package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public record ConnectingFrom(BlockPos pos, Vec3d axis, Vec3d normal, Vec3d end) {
    public static final Codec<ConnectingFrom> CODEC = RecordCodecBuilder.create(i -> i.group(
        BlockPos.CODEC.fieldOf("pos").forGetter(ConnectingFrom::pos),
        Vec3d.CODEC.fieldOf("axis").forGetter(ConnectingFrom::axis),
        Vec3d.CODEC.fieldOf("normal").forGetter(ConnectingFrom::normal),
        Vec3d.CODEC.fieldOf("end").forGetter(ConnectingFrom::end)
    ).apply(i, ConnectingFrom::new));

    public static final PacketCodec<PacketByteBuf, ConnectingFrom> STREAM_CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        ConnectingFrom::pos,
        Vec3d.PACKET_CODEC,
        ConnectingFrom::axis,
        Vec3d.PACKET_CODEC,
        ConnectingFrom::normal,
        Vec3d.PACKET_CODEC,
        ConnectingFrom::end,
        ConnectingFrom::new
    );
}
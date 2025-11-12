package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public record ConnectingFrom(BlockPos pos, Vec3 axis, Vec3 normal, Vec3 end) {
    public static final Codec<ConnectingFrom> CODEC = RecordCodecBuilder.create(i -> i.group(
        BlockPos.CODEC.fieldOf("pos").forGetter(ConnectingFrom::pos),
        Vec3.CODEC.fieldOf("axis").forGetter(ConnectingFrom::axis),
        Vec3.CODEC.fieldOf("normal").forGetter(ConnectingFrom::normal),
        Vec3.CODEC.fieldOf("end").forGetter(ConnectingFrom::end)
    ).apply(i, ConnectingFrom::new));

    public static final StreamCodec<FriendlyByteBuf, ConnectingFrom> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        ConnectingFrom::pos,
        Vec3.STREAM_CODEC,
        ConnectingFrom::axis,
        Vec3.STREAM_CODEC,
        ConnectingFrom::normal,
        Vec3.STREAM_CODEC,
        ConnectingFrom::end,
        ConnectingFrom::new
    );
}
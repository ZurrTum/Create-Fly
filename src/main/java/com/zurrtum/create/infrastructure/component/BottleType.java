package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;

import java.util.Locale;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum BottleType implements StringRepresentable {
    REGULAR,
    SPLASH,
    LINGERING;

    public static final Codec<BottleType> CODEC = StringRepresentable.fromEnum(BottleType::values);
    public static final StreamCodec<ByteBuf, BottleType> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(BottleType.class);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
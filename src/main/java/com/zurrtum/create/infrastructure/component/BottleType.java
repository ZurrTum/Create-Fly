package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum BottleType implements StringIdentifiable {
    REGULAR,
    SPLASH,
    LINGERING;

    public static final Codec<BottleType> CODEC = StringIdentifiable.createCodec(BottleType::values);
    public static final PacketCodec<ByteBuf, BottleType> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(BottleType.class);

    @Override
    public @NotNull String asString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
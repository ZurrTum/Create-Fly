package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum AttributeFilterWhitelistMode implements StringIdentifiable {
    WHITELIST_DISJ,
    WHITELIST_CONJ,
    BLACKLIST;

    public static final Codec<AttributeFilterWhitelistMode> CODEC = StringIdentifiable.createCodec(AttributeFilterWhitelistMode::values);
    public static final PacketCodec<ByteBuf, AttributeFilterWhitelistMode> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(
        AttributeFilterWhitelistMode.class);

    @Override
    public @NotNull String asString() {
        return name().toLowerCase(Locale.ROOT);
    }
}

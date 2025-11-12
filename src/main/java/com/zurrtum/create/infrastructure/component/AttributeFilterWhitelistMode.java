package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum AttributeFilterWhitelistMode implements StringRepresentable {
    WHITELIST_DISJ,
    WHITELIST_CONJ,
    BLACKLIST;

    public static final Codec<AttributeFilterWhitelistMode> CODEC = StringRepresentable.fromEnum(AttributeFilterWhitelistMode::values);
    public static final StreamCodec<ByteBuf, AttributeFilterWhitelistMode> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(
        AttributeFilterWhitelistMode.class);

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}

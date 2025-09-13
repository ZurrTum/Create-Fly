package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum PlacementOptions implements StringIdentifiable {
    Merged,
    Attached,
    Inserted;

    public static final Codec<PlacementOptions> CODEC = StringIdentifiable.createCodec(PlacementOptions::values);
    public static final PacketCodec<ByteBuf, PlacementOptions> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(PlacementOptions.class);

    public final String translationKey;

    PlacementOptions() {
        this.translationKey = name().toLowerCase(Locale.ROOT);
    }

    @Override
    public @NotNull String asString() {
        return translationKey;
    }
}

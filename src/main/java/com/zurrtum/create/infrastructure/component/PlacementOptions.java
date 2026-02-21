package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum PlacementOptions implements StringRepresentable {
    Merged, Attached, Inserted;

    public static final Codec<PlacementOptions> CODEC = StringRepresentable.fromEnum(PlacementOptions::values);
    public static final StreamCodec<ByteBuf, PlacementOptions> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(
        PlacementOptions.class);

    public final String translationKey;

    PlacementOptions() {
        this.translationKey = name().toLowerCase(Locale.ROOT);
    }

    @Override
    public @NotNull String getSerializedName() {
        return translationKey;
    }
}

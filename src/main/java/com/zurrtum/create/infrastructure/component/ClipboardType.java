package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;

public enum ClipboardType implements StringRepresentable {
    EMPTY("empty_clipboard"),
    WRITTEN("clipboard"),
    EDITING("clipboard_and_quill");

    public static final Codec<ClipboardType> CODEC = StringRepresentable.fromEnum(ClipboardType::values);
    public static final StreamCodec<ByteBuf, ClipboardType> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(ClipboardType.class);

    public final String file;
    public static Identifier ID = Identifier.parse("clipboard_type");

    ClipboardType(String file) {
        this.file = file;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}

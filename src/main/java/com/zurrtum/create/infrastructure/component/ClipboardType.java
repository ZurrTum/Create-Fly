package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum ClipboardType implements StringIdentifiable {
    EMPTY("empty_clipboard"),
    WRITTEN("clipboard"),
    EDITING("clipboard_and_quill");

    public static final Codec<ClipboardType> CODEC = StringIdentifiable.createCodec(ClipboardType::values);
    public static final PacketCodec<ByteBuf, ClipboardType> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(ClipboardType.class);

    public final String file;
    public static Identifier ID = Identifier.of("clipboard_type");

    ClipboardType(String file) {
        this.file = file;
    }

    @Override
    public @NotNull String asString() {
        return name().toLowerCase(Locale.ROOT);
    }
}

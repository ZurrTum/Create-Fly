package com.zurrtum.create.content.logistics.factoryBoard;

import com.mojang.serialization.Codec;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum PanelSlot implements StringIdentifiable {
    TOP_LEFT(1, 1),
    TOP_RIGHT(0, 1),
    BOTTOM_LEFT(1, 0),
    BOTTOM_RIGHT(0, 0);

    public static final Codec<PanelSlot> CODEC = StringIdentifiable.createCodec(PanelSlot::values);
    public static final PacketCodec<ByteBuf, PanelSlot> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(PanelSlot.class);

    public final int xOffset;
    public final int yOffset;

    PanelSlot(int xOffset, int yOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    @Override
    public @NotNull String asString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
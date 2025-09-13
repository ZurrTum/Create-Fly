package com.zurrtum.create.content.trains.signal;

import com.mojang.serialization.Codec;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.catnip.theme.Color;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.StringIdentifiable;

import java.util.Locale;

public enum EdgeGroupColor implements StringIdentifiable {

    YELLOW(0xEBC255),
    GREEN(0x51C054),
    BLUE(0x5391E1),
    ORANGE(0xE36E36),
    LAVENDER(0xCB92BA),
    RED(0xA43538),
    CYAN(0x6EDAD9),
    BROWN(0xA17C58),

    WHITE(0xE5E1DC);

    public static final Codec<EdgeGroupColor> CODEC = StringIdentifiable.createCodec(EdgeGroupColor::values);
    public static final PacketCodec<ByteBuf, EdgeGroupColor> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(EdgeGroupColor.class);

    private final Color color;
    private final int mask;

    EdgeGroupColor(int color) {
        this.color = new Color(color);
        mask = 1 << ordinal();
    }

    public int strikeFrom(int mask) {
        if (this == WHITE)
            return mask;
        return mask | this.mask;
    }

    public Color get() {
        return color;
    }

    public static EdgeGroupColor getDefault() {
        return values()[0];
    }

    public static EdgeGroupColor findNextAvailable(int mask) {
        EdgeGroupColor[] values = values();
        for (EdgeGroupColor value : values) {
            if ((mask & 1) == 0)
                return value;
            mask = mask >> 1;
        }
        return WHITE;
    }

    @Override
    public String asString() {
        return name().toLowerCase(Locale.ROOT);
    }
}

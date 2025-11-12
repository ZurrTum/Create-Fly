package com.zurrtum.create.content.logistics.factoryBoard;

import com.mojang.serialization.Codec;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum PanelSlot implements StringRepresentable {
    TOP_LEFT(1, 1),
    TOP_RIGHT(0, 1),
    BOTTOM_LEFT(1, 0),
    BOTTOM_RIGHT(0, 0);

    public static final Codec<PanelSlot> CODEC = StringRepresentable.fromEnum(PanelSlot::values);
    public static final StreamCodec<ByteBuf, PanelSlot> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(PanelSlot.class);

    public final int xOffset;
    public final int yOffset;

    PanelSlot(int xOffset, int yOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
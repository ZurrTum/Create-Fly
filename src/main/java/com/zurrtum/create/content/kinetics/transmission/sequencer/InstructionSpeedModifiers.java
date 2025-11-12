package com.zurrtum.create.content.kinetics.transmission.sequencer;

import com.mojang.serialization.Codec;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;

import java.util.Locale;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum InstructionSpeedModifiers implements StringRepresentable {
    FORWARD_FAST(2),
    FORWARD(1),
    BACK(-1),
    BACK_FAST(-2);

    public static final Codec<InstructionSpeedModifiers> CODEC = StringRepresentable.fromEnum(InstructionSpeedModifiers::values);
    public static final StreamCodec<ByteBuf, InstructionSpeedModifiers> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(InstructionSpeedModifiers.class);

    public final int value;

    InstructionSpeedModifiers(int modifier) {
        this.value = modifier;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
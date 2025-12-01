package com.zurrtum.create.content.kinetics.transmission.sequencer;

import com.mojang.serialization.Codec;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.StringIdentifiable;

import java.util.Arrays;
import java.util.Locale;

public enum InstructionSpeedModifiers implements StringIdentifiable {
    FORWARD_FAST(2),
    FORWARD(1),
    BACK(-1),
    BACK_FAST(-2);

    public static final Codec<InstructionSpeedModifiers> CODEC = StringIdentifiable.createCodec(InstructionSpeedModifiers::values);
    public static final PacketCodec<ByteBuf, InstructionSpeedModifiers> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(InstructionSpeedModifiers.class);

    public final int value;

    InstructionSpeedModifiers(int modifier) {
        this.value = modifier;
    }

    @Override
    public String asString() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static InstructionSpeedModifiers getByModifier(int modifier) {
        return Arrays.stream(InstructionSpeedModifiers.values())
                .filter(speedModifier -> speedModifier.value == modifier)
                .findAny()
                .orElse(InstructionSpeedModifiers.FORWARD);
    }
}
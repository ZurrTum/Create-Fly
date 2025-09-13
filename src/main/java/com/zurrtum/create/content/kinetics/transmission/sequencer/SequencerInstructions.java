package com.zurrtum.create.content.kinetics.transmission.sequencer;

import com.mojang.serialization.Codec;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.StringIdentifiable;

import java.util.Locale;

public enum SequencerInstructions implements StringIdentifiable {
    TURN_ANGLE,
    TURN_DISTANCE,
    DELAY,
    AWAIT,
    END;

    public static final Codec<SequencerInstructions> CODEC = StringIdentifiable.createCodec(SequencerInstructions::values);
    public static final PacketCodec<ByteBuf, SequencerInstructions> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(SequencerInstructions.class);

    @Override
    public String asString() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean needsPropagation() {
        return this == TURN_ANGLE || this == TURN_DISTANCE;
    }
}

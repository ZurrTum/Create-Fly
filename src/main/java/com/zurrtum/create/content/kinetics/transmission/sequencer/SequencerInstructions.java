package com.zurrtum.create.content.kinetics.transmission.sequencer;

import com.mojang.serialization.Codec;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum SequencerInstructions implements StringRepresentable {
    TURN_ANGLE,
    TURN_DISTANCE,
    DELAY,
    AWAIT,
    END;

    public static final Codec<SequencerInstructions> CODEC = StringRepresentable.fromEnum(SequencerInstructions::values);
    public static final StreamCodec<ByteBuf, SequencerInstructions> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(SequencerInstructions.class);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean needsPropagation() {
        return this == TURN_ANGLE || this == TURN_DISTANCE;
    }
}

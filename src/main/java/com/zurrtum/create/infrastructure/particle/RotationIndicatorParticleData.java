package com.zurrtum.create.infrastructure.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllParticleTypes;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.Direction;

public record RotationIndicatorParticleData(
    int color, float speed, float radius1, float radius2, int lifeSpan, Direction.Axis axis
) implements ParticleEffect {

    public static final MapCodec<RotationIndicatorParticleData> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.INT.fieldOf("color").forGetter(RotationIndicatorParticleData::color),
        Codec.FLOAT.fieldOf("speed").forGetter(RotationIndicatorParticleData::speed),
        Codec.FLOAT.fieldOf("radius1").forGetter(RotationIndicatorParticleData::radius1),
        Codec.FLOAT.fieldOf("radius2").forGetter(RotationIndicatorParticleData::radius2),
        Codec.INT.fieldOf("life_span").forGetter(RotationIndicatorParticleData::lifeSpan),
        Direction.Axis.CODEC.fieldOf("axis").forGetter(RotationIndicatorParticleData::axis)
    ).apply(i, RotationIndicatorParticleData::new));

    public static final PacketCodec<RegistryByteBuf, RotationIndicatorParticleData> STREAM_CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER,
        RotationIndicatorParticleData::color,
        PacketCodecs.FLOAT,
        RotationIndicatorParticleData::speed,
        PacketCodecs.FLOAT,
        RotationIndicatorParticleData::radius1,
        PacketCodecs.FLOAT,
        RotationIndicatorParticleData::radius2,
        PacketCodecs.INTEGER,
        RotationIndicatorParticleData::lifeSpan,
        CatnipStreamCodecs.AXIS,
        RotationIndicatorParticleData::axis,
        RotationIndicatorParticleData::new
    );

    @Override
    public ParticleType<RotationIndicatorParticleData> getType() {
        return AllParticleTypes.ROTATION_INDICATOR;
    }
}
package com.zurrtum.create.infrastructure.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllParticleTypes;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record RotationIndicatorParticleData(
    int color, float speed, float radius1, float radius2, int lifeSpan, Direction.Axis axis
) implements ParticleOptions {

    public static final MapCodec<RotationIndicatorParticleData> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.INT.fieldOf("color").forGetter(RotationIndicatorParticleData::color),
        Codec.FLOAT.fieldOf("speed").forGetter(RotationIndicatorParticleData::speed),
        Codec.FLOAT.fieldOf("radius1").forGetter(RotationIndicatorParticleData::radius1),
        Codec.FLOAT.fieldOf("radius2").forGetter(RotationIndicatorParticleData::radius2),
        Codec.INT.fieldOf("life_span").forGetter(RotationIndicatorParticleData::lifeSpan),
        Direction.Axis.CODEC.fieldOf("axis").forGetter(RotationIndicatorParticleData::axis)
    ).apply(i, RotationIndicatorParticleData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, RotationIndicatorParticleData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        RotationIndicatorParticleData::color,
        ByteBufCodecs.FLOAT,
        RotationIndicatorParticleData::speed,
        ByteBufCodecs.FLOAT,
        RotationIndicatorParticleData::radius1,
        ByteBufCodecs.FLOAT,
        RotationIndicatorParticleData::radius2,
        ByteBufCodecs.INT,
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
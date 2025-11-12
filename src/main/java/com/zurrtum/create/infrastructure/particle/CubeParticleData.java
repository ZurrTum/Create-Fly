package com.zurrtum.create.infrastructure.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record CubeParticleData(float red, float green, float blue, float scale, int avgAge, boolean hot) implements ParticleOptions {

    public static final MapCodec<CubeParticleData> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.FLOAT.fieldOf("r").forGetter(CubeParticleData::red),
        Codec.FLOAT.fieldOf("g").forGetter(CubeParticleData::green),
        Codec.FLOAT.fieldOf("b").forGetter(CubeParticleData::blue),
        Codec.FLOAT.fieldOf("scale").forGetter(CubeParticleData::scale),
        Codec.INT.fieldOf("avg_age").forGetter(CubeParticleData::avgAge),
        Codec.BOOL.fieldOf("hot").forGetter(CubeParticleData::hot)
    ).apply(i, CubeParticleData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CubeParticleData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT,
        CubeParticleData::red,
        ByteBufCodecs.FLOAT,
        CubeParticleData::green,
        ByteBufCodecs.FLOAT,
        CubeParticleData::blue,
        ByteBufCodecs.FLOAT,
        CubeParticleData::scale,
        ByteBufCodecs.INT,
        CubeParticleData::avgAge,
        ByteBufCodecs.BOOL,
        CubeParticleData::hot,
        CubeParticleData::new
    );

    @Override
    public ParticleType<CubeParticleData> getType() {
        return AllParticleTypes.CUBE;
    }
}

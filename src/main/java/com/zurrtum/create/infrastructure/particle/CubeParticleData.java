package com.zurrtum.create.infrastructure.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllParticleTypes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public record CubeParticleData(float red, float green, float blue, float scale, int avgAge, boolean hot) implements ParticleEffect {

    public static final MapCodec<CubeParticleData> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.FLOAT.fieldOf("r").forGetter(CubeParticleData::red),
        Codec.FLOAT.fieldOf("g").forGetter(CubeParticleData::green),
        Codec.FLOAT.fieldOf("b").forGetter(CubeParticleData::blue),
        Codec.FLOAT.fieldOf("scale").forGetter(CubeParticleData::scale),
        Codec.INT.fieldOf("avg_age").forGetter(CubeParticleData::avgAge),
        Codec.BOOL.fieldOf("hot").forGetter(CubeParticleData::hot)
    ).apply(i, CubeParticleData::new));

    public static final PacketCodec<RegistryByteBuf, CubeParticleData> STREAM_CODEC = PacketCodec.tuple(
        PacketCodecs.FLOAT,
        CubeParticleData::red,
        PacketCodecs.FLOAT,
        CubeParticleData::green,
        PacketCodecs.FLOAT,
        CubeParticleData::blue,
        PacketCodecs.FLOAT,
        CubeParticleData::scale,
        PacketCodecs.INTEGER,
        CubeParticleData::avgAge,
        PacketCodecs.BOOLEAN,
        CubeParticleData::hot,
        CubeParticleData::new
    );

    @Override
    public ParticleType<CubeParticleData> getType() {
        return AllParticleTypes.CUBE;
    }
}

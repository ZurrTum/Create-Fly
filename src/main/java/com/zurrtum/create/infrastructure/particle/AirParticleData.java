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

public record AirParticleData(float drag, float speed) implements ParticleOptions {

    public static final MapCodec<AirParticleData> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.FLOAT.fieldOf("drag").forGetter(AirParticleData::drag), Codec.FLOAT.fieldOf("speed").forGetter(AirParticleData::speed))
        .apply(i, AirParticleData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AirParticleData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT,
        AirParticleData::drag,
        ByteBufCodecs.FLOAT,
        AirParticleData::speed,
        AirParticleData::new
    );

    @Override
    public ParticleType<AirParticleData> getType() {
        return AllParticleTypes.AIR;
    }
}

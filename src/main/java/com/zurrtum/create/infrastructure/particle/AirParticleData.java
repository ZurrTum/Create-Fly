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
import org.jetbrains.annotations.NotNull;

public record AirParticleData(float drag, float speed) implements ParticleEffect {

    public static final MapCodec<AirParticleData> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.FLOAT.fieldOf("drag").forGetter(AirParticleData::drag), Codec.FLOAT.fieldOf("speed").forGetter(AirParticleData::speed))
        .apply(i, AirParticleData::new));

    public static final PacketCodec<RegistryByteBuf, AirParticleData> STREAM_CODEC = PacketCodec.tuple(
        PacketCodecs.FLOAT,
        AirParticleData::drag,
        PacketCodecs.FLOAT,
        AirParticleData::speed,
        AirParticleData::new
    );

    @Override
    public @NotNull ParticleType<AirParticleData> getType() {
        return AllParticleTypes.AIR;
    }
}

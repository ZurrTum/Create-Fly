package com.zurrtum.create.infrastructure.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllParticleTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public record SteamJetParticleData(float speed) implements ParticleEffect {

    public static final MapCodec<SteamJetParticleData> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(Codec.FLOAT.fieldOf("speed")
        .forGetter(SteamJetParticleData::speed)).apply(i, SteamJetParticleData::new));

    public static final PacketCodec<ByteBuf, SteamJetParticleData> STREAM_CODEC = PacketCodecs.FLOAT.xmap(
        SteamJetParticleData::new,
        SteamJetParticleData::speed
    );

    @Override
    public ParticleType<SteamJetParticleData> getType() {
        return AllParticleTypes.STEAM_JET;
    }
}
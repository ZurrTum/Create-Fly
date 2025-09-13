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
import net.minecraft.util.math.Vec3i;

public record AirFlowParticleData(int posX, int posY, int posZ) implements ParticleEffect {
    public static final MapCodec<AirFlowParticleData> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.INT.fieldOf("x").forGetter(AirFlowParticleData::posX),
        Codec.INT.fieldOf("y").forGetter(AirFlowParticleData::posY),
        Codec.INT.fieldOf("z").forGetter(AirFlowParticleData::posZ)
    ).apply(i, AirFlowParticleData::new));

    public static final PacketCodec<RegistryByteBuf, AirFlowParticleData> STREAM_CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER,
        AirFlowParticleData::posX,
        PacketCodecs.INTEGER,
        AirFlowParticleData::posY,
        PacketCodecs.INTEGER,
        AirFlowParticleData::posZ,
        AirFlowParticleData::new
    );

    public AirFlowParticleData(Vec3i pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public ParticleType<AirFlowParticleData> getType() {
        return AllParticleTypes.AIR_FLOW;
    }
}
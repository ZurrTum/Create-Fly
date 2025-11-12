package com.zurrtum.create.infrastructure.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllParticleTypes;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record AirFlowParticleData(int posX, int posY, int posZ) implements ParticleOptions {
    public static final MapCodec<AirFlowParticleData> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.INT.fieldOf("x").forGetter(AirFlowParticleData::posX),
        Codec.INT.fieldOf("y").forGetter(AirFlowParticleData::posY),
        Codec.INT.fieldOf("z").forGetter(AirFlowParticleData::posZ)
    ).apply(i, AirFlowParticleData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AirFlowParticleData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        AirFlowParticleData::posX,
        ByteBufCodecs.INT,
        AirFlowParticleData::posY,
        ByteBufCodecs.INT,
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
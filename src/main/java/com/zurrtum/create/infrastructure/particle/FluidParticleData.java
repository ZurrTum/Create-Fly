package com.zurrtum.create.infrastructure.particle;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllParticleTypes;
import net.minecraft.fluid.Fluid;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;

public record FluidParticleData(ParticleType<FluidParticleData> type, Fluid fluid) implements ParticleEffect {

    private static final RecordCodecBuilder<FluidParticleData, Fluid> FLUID_CODEC = Registries.FLUID.getCodec().fieldOf("fluid")
        .forGetter(FluidParticleData::fluid);
    public static final MapCodec<FluidParticleData> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(FLUID_CODEC)
        .apply(i, FluidParticleData::particle));
    public static final MapCodec<FluidParticleData> BASIN_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(FLUID_CODEC)
        .apply(i, FluidParticleData::basin));
    public static final MapCodec<FluidParticleData> DRIP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(FLUID_CODEC)
        .apply(i, FluidParticleData::drip));

    private static final PacketCodec<RegistryByteBuf, Fluid> FLUID_STREAM_CODEC = PacketCodecs.registryValue(RegistryKeys.FLUID);
    public static final PacketCodec<RegistryByteBuf, FluidParticleData> STREAM_CODEC = PacketCodec.tuple(
        FLUID_STREAM_CODEC,
        FluidParticleData::fluid,
        FluidParticleData::particle
    );
    public static final PacketCodec<RegistryByteBuf, FluidParticleData> BASIN_STREAM_CODEC = PacketCodec.tuple(
        FLUID_STREAM_CODEC,
        FluidParticleData::fluid,
        FluidParticleData::basin
    );
    public static final PacketCodec<RegistryByteBuf, FluidParticleData> DRIP_STREAM_CODEC = PacketCodec.tuple(
        FLUID_STREAM_CODEC,
        FluidParticleData::fluid,
        FluidParticleData::drip
    );

    public static FluidParticleData particle(Fluid fluid) {
        return new FluidParticleData(AllParticleTypes.FLUID_PARTICLE, fluid);
    }

    public static FluidParticleData basin(Fluid fluid) {
        return new FluidParticleData(AllParticleTypes.BASIN_FLUID, fluid);
    }

    public static FluidParticleData drip(Fluid fluid) {
        return new FluidParticleData(AllParticleTypes.FLUID_DRIP, fluid);
    }

    @Override
    public ParticleType<FluidParticleData> getType() {
        return type;
    }
}

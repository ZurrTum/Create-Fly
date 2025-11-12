package com.zurrtum.create.infrastructure.particle;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllParticleTypes;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;

public record FluidParticleData(ParticleType<FluidParticleData> type, Fluid fluid, DataComponentPatch components) implements ParticleOptions {

    private static final RecordCodecBuilder<FluidParticleData, Fluid> FLUID_CODEC = BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid")
        .forGetter(FluidParticleData::fluid);
    private static final RecordCodecBuilder<FluidParticleData, DataComponentPatch> COMPONENTS_CODEC = DataComponentPatch.CODEC.fieldOf("components")
        .forGetter(FluidParticleData::components);
    public static final MapCodec<FluidParticleData> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(FLUID_CODEC, COMPONENTS_CODEC)
        .apply(i, FluidParticleData::particle));
    public static final MapCodec<FluidParticleData> BASIN_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(FLUID_CODEC, COMPONENTS_CODEC)
        .apply(i, FluidParticleData::basin));
    public static final MapCodec<FluidParticleData> DRIP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(FLUID_CODEC, COMPONENTS_CODEC)
        .apply(i, FluidParticleData::drip));

    private static final StreamCodec<RegistryFriendlyByteBuf, Fluid> FLUID_STREAM_CODEC = ByteBufCodecs.registry(Registries.FLUID);
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidParticleData> STREAM_CODEC = StreamCodec.composite(
        FLUID_STREAM_CODEC,
        FluidParticleData::fluid,
        DataComponentPatch.STREAM_CODEC,
        FluidParticleData::components,
        FluidParticleData::particle
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidParticleData> BASIN_STREAM_CODEC = StreamCodec.composite(
        FLUID_STREAM_CODEC,
        FluidParticleData::fluid,
        DataComponentPatch.STREAM_CODEC,
        FluidParticleData::components,
        FluidParticleData::basin
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidParticleData> DRIP_STREAM_CODEC = StreamCodec.composite(
        FLUID_STREAM_CODEC,
        FluidParticleData::fluid,
        DataComponentPatch.STREAM_CODEC,
        FluidParticleData::components,
        FluidParticleData::drip
    );

    public FluidParticleData(ParticleType<FluidParticleData> type, Fluid fluid) {
        this(type, fluid, DataComponentPatch.EMPTY);
    }

    public static FluidParticleData particle(Fluid fluid, DataComponentPatch components) {
        return new FluidParticleData(AllParticleTypes.FLUID_PARTICLE, fluid, components);
    }

    public static FluidParticleData basin(Fluid fluid, DataComponentPatch components) {
        return new FluidParticleData(AllParticleTypes.BASIN_FLUID, fluid, components);
    }

    public static FluidParticleData drip(Fluid fluid, DataComponentPatch components) {
        return new FluidParticleData(AllParticleTypes.FLUID_DRIP, fluid, components);
    }

    @Override
    public ParticleType<FluidParticleData> getType() {
        return type;
    }
}

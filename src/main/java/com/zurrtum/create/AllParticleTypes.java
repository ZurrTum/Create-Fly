package com.zurrtum.create;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.infrastructure.particle.*;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllParticleTypes {
    public static final ParticleType<RotationIndicatorParticleData> ROTATION_INDICATOR = register(
        "rotation_indicator",
        RotationIndicatorParticleData.CODEC,
        RotationIndicatorParticleData.STREAM_CODEC
    );
    public static final ParticleType<AirFlowParticleData> AIR_FLOW = register(
        "air_flow",
        AirFlowParticleData.CODEC,
        AirFlowParticleData.STREAM_CODEC
    );
    public static final ParticleType<AirParticleData> AIR = register("air", AirParticleData.CODEC, AirParticleData.STREAM_CODEC);
    public static final SimpleParticleType STEAM_JET = register("steam_jet");
    public static final ParticleType<CubeParticleData> CUBE = register("cube", CubeParticleData.CODEC, CubeParticleData.STREAM_CODEC);
    public static final ParticleType<FluidParticleData> FLUID_PARTICLE = register(
        "fluid_particle",
        FluidParticleData.CODEC,
        FluidParticleData.STREAM_CODEC
    );
    public static final ParticleType<FluidParticleData> BASIN_FLUID = register(
        "basin_fluid",
        FluidParticleData.BASIN_CODEC,
        FluidParticleData.BASIN_STREAM_CODEC
    );
    public static final ParticleType<FluidParticleData> FLUID_DRIP = register(
        "fluid_drip",
        FluidParticleData.DRIP_CODEC,
        FluidParticleData.DRIP_STREAM_CODEC
    );
    public static final SimpleParticleType WIFI = register("wifi");
    public static final SimpleParticleType SOUL = register("soul");
    public static final SimpleParticleType SOUL_BASE = register("soul_base");
    public static final SimpleParticleType SOUL_PERIMETER = register("soul_perimeter");
    public static final SimpleParticleType SOUL_EXPANDING_PERIMETER = register("soul_expanding_perimeter");

    private static SimpleParticleType register(String name) {
        Identifier id = Identifier.of(MOD_ID, name);
        return Registry.register(Registries.PARTICLE_TYPE, id, new SimpleParticleType(false));
    }

    private static <T extends ParticleEffect> ParticleType<T> register(
        String name,
        MapCodec<T> codec,
        PacketCodec<? super RegistryByteBuf, T> packetCodec
    ) {
        ParticleType<T> type = new ParticleType<T>(false) {
            public MapCodec<T> getCodec() {
                return codec;
            }

            public PacketCodec<? super RegistryByteBuf, T> getPacketCodec() {
                return packetCodec;
            }
        };
        return Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MOD_ID, name), type);
    }

    public static void register() {
    }
}

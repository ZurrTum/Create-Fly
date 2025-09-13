package com.zurrtum.create.client;

import com.zurrtum.create.client.infrastructure.particle.*;
import net.minecraft.client.particle.ParticleManager;

import static com.zurrtum.create.AllParticleTypes.*;

public class AllParticleTypes {
    public static void register(ParticleManager particle) {
        particle.registerFactory(ROTATION_INDICATOR, RotationIndicatorParticle.Factory::new);
        particle.registerFactory(AIR_FLOW, AirFlowParticle.Factory::new);
        particle.registerFactory(AIR, AirParticle.Factory::new);
        particle.registerFactory(STEAM_JET, SteamJetParticle.Factory::new);
        particle.registerFactory(CUBE, new CubeParticle.Factory());
        particle.registerFactory(FLUID_PARTICLE, new FluidParticle.Factory());
        particle.registerFactory(FLUID_DRIP, new FluidParticle.Factory());
        particle.registerFactory(BASIN_FLUID, new BasinFluidParticle.Factory());
        particle.registerFactory(WIFI, BasicParticleFactory::wifi);
        particle.registerFactory(SOUL, BasicParticleFactory::soul);
        particle.registerFactory(SOUL_BASE, BasicParticleFactory::soulBase);
        particle.registerFactory(SOUL_PERIMETER, BasicParticleFactory::soul);
        particle.registerFactory(SOUL_EXPANDING_PERIMETER, BasicParticleFactory::soul);
    }
}

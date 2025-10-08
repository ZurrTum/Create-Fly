package com.zurrtum.create.client;

import com.zurrtum.create.client.infrastructure.particle.*;
import net.minecraft.client.particle.ParticleSpriteManager;

import static com.zurrtum.create.AllParticleTypes.*;

public class AllParticleTypes {
    public static void register(ParticleSpriteManager particle) {
        particle.register(ROTATION_INDICATOR, RotationIndicatorParticle.Factory::new);
        particle.register(AIR_FLOW, AirFlowParticle.Factory::new);
        particle.register(AIR, AirParticle.Factory::new);
        particle.register(STEAM_JET, SteamJetParticle.Factory::new);
        particle.register(CUBE, new CubeParticle.Factory());
        particle.register(FLUID_PARTICLE, new FluidParticle.Factory());
        particle.register(FLUID_DRIP, new FluidParticle.Factory());
        particle.register(BASIN_FLUID, new BasinFluidParticle.Factory());
        particle.register(WIFI, BasicParticleFactory::wifi);
        particle.register(SOUL, BasicParticleFactory::soul);
        particle.register(SOUL_BASE, BasicParticleFactory::soulBase);
        particle.register(SOUL_PERIMETER, BasicParticleFactory::soul);
        particle.register(SOUL_EXPANDING_PERIMETER, BasicParticleFactory::soul);
    }
}

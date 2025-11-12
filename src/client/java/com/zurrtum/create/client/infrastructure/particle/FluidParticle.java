package com.zurrtum.create.client.infrastructure.particle;

import com.zurrtum.create.AllFluids;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.infrastructure.fluid.FluidConfig;
import com.zurrtum.create.infrastructure.particle.FluidParticleData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.material.Fluid;

public class FluidParticle extends SingleQuadParticle {
    private final float uo;
    private final float vo;
    private final Fluid fluid;
    private final DataComponentPatch components;
    private final FluidConfig config;

    public FluidParticle(
        ClientLevel world,
        Fluid fluid,
        DataComponentPatch components,
        FluidConfig config,
        double x,
        double y,
        double z,
        double vx,
        double vy,
        double vz,
        RandomSource random
    ) {
        super(world, x, y, z, vx, vy, vz, config.still().get());

        this.fluid = fluid;
        this.components = components;
        this.config = config;

        this.gravity = 1.0F;
        this.updateColor();
        this.multiplyColor(config.tint().apply(components));

        this.xd = vx;
        this.yd = vy;
        this.zd = vz;

        this.quadSize /= 2.0F;
        this.uo = random.nextFloat() * 3.0F;
        this.vo = random.nextFloat() * 3.0F;
    }

    @Override
    protected int getLightColor(float p_189214_1_) {
        int brightnessForRender = super.getLightColor(p_189214_1_);
        int skyLight = brightnessForRender >> 20;
        int blockLight = (brightnessForRender >> 4) & 0xf;
        blockLight = Math.max(blockLight, fluid.defaultFluidState().createLegacyBlock().getLightEmission());
        return (skyLight << 20) | (blockLight << 4);
    }

    protected void updateColor() {
        this.rCol = 0.8F;
        this.gCol = 0.8F;
        this.bCol = 0.8F;
    }

    protected void multiplyColor(int color) {
        this.rCol *= (float) (color >> 16 & 255) / 255.0F;
        this.gCol *= (float) (color >> 8 & 255) / 255.0F;
        this.bCol *= (float) (color & 255) / 255.0F;
    }

    @Override
    protected float getU0() {
        return this.sprite.getU((this.uo + 1.0F) / 4.0F);
    }

    @Override
    protected float getU1() {
        return this.sprite.getU(this.uo / 4.0F);
    }

    @Override
    protected float getV0() {
        return this.sprite.getV(this.vo / 4.0F);
    }

    @Override
    protected float getV1() {
        return this.sprite.getV((this.vo + 1.0F) / 4.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (!canEvaporate())
            return;
        if (onGround)
            remove();
        if (!removed)
            return;
        if (!onGround && random.nextFloat() < 1 / 8f)
            return;

        Color color = new Color(config.tint().apply(components));
        level.addParticle(
            ColorParticleOption.create(
                ParticleTypes.ENTITY_EFFECT,
                color.getRedAsFloat(),
                color.getGreenAsFloat(),
                color.getBlueAsFloat()
            ),
            x,
            y,
            z,
            0,
            0,
            0
        );
    }

    protected boolean canEvaporate() {
        return fluid == AllFluids.POTION;
    }

    @Override
    protected Layer getLayer() {
        return SingleQuadParticle.Layer.TERRAIN;
    }

    public static class Factory implements ParticleProvider<FluidParticleData> {
        @Override
        public Particle createParticle(
            FluidParticleData data,
            ClientLevel world,
            double x,
            double y,
            double z,
            double vx,
            double vy,
            double vz,
            RandomSource random
        ) {
            FluidConfig config = AllFluidConfigs.get(data.fluid());
            if (config == null) {
                return null;
            }
            return new FluidParticle(world, data.fluid(), data.components(), config, x, y, z, vx, vy, vz, random);
        }
    }
}

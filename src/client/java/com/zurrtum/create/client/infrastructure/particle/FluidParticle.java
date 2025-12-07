package com.zurrtum.create.client.infrastructure.particle;

import com.zurrtum.create.AllFluids;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.infrastructure.fluid.FluidConfig;
import com.zurrtum.create.infrastructure.particle.FluidParticleData;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.ComponentChanges;
import net.minecraft.fluid.Fluid;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.TintedParticleEffect;
import org.jetbrains.annotations.NotNull;

public class FluidParticle extends SpriteBillboardParticle {
    private final float uo;
    private final float vo;
    private final Fluid fluid;
    private final ComponentChanges components;
    private final FluidConfig config;

    public FluidParticle(
        ClientWorld world,
        Fluid fluid,
        ComponentChanges components,
        FluidConfig config,
        double x,
        double y,
        double z,
        double vx,
        double vy,
        double vz
    ) {
        super(world, x, y, z, vx, vy, vz);

        this.fluid = fluid;
        this.components = components;
        this.config = config;
        this.setSprite(config.still().get());

        this.gravityStrength = 1.0F;
        this.updateColor();
        this.multiplyColor(config.tint().apply(components));

        this.velocityX = vx;
        this.velocityY = vy;
        this.velocityZ = vz;

        this.scale /= 2.0F;
        this.uo = this.random.nextFloat() * 3.0F;
        this.vo = this.random.nextFloat() * 3.0F;
    }

    @Override
    protected int getBrightness(float p_189214_1_) {
        int brightnessForRender = super.getBrightness(p_189214_1_);
        int skyLight = brightnessForRender >> 20;
        int blockLight = (brightnessForRender >> 4) & 0xf;
        blockLight = Math.max(blockLight, fluid.getDefaultState().getBlockState().getLuminance());
        return (skyLight << 20) | (blockLight << 4);
    }

    protected void updateColor() {
        this.red = 0.8F;
        this.green = 0.8F;
        this.blue = 0.8F;
    }

    protected void multiplyColor(int color) {
        this.red *= (float) (color >> 16 & 255) / 255.0F;
        this.green *= (float) (color >> 8 & 255) / 255.0F;
        this.blue *= (float) (color & 255) / 255.0F;
    }

    @Override
    protected float getMinU() {
        return this.sprite.getFrameU((this.uo + 1.0F) / 4.0F);
    }

    @Override
    protected float getMaxU() {
        return this.sprite.getFrameU(this.uo / 4.0F);
    }

    @Override
    protected float getMinV() {
        return this.sprite.getFrameV(this.vo / 4.0F);
    }

    @Override
    protected float getMaxV() {
        return this.sprite.getFrameV((this.vo + 1.0F) / 4.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (!canEvaporate())
            return;
        if (onGround)
            markDead();
        if (!dead)
            return;
        if (!onGround && world.random.nextFloat() < 1 / 8f)
            return;

        Color color = new Color(config.tint().apply(components));
        world.addParticleClient(
            TintedParticleEffect.create(
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
    public @NotNull ParticleTextureSheet getType() {
        return ParticleTextureSheet.TERRAIN_SHEET;
    }

    public static class Factory implements ParticleFactory<FluidParticleData> {
        @Override
        public Particle createParticle(FluidParticleData data, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
            FluidConfig config = AllFluidConfigs.get(data.fluid());
            if (config == null) {
                return null;
            }
            return new FluidParticle(world, data.fluid(), data.components(), config, x, y, z, vx, vy, vz);
        }
    }
}

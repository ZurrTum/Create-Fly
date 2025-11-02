package com.zurrtum.create.client.infrastructure.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import org.joml.Quaternionf;

public class SteamJetParticle extends AnimatedParticle {
    private final float yaw;
    private final float pitch;

    protected SteamJetParticle(
        ClientWorld world,
        double x,
        double y,
        double z,
        double dx,
        double dy,
        double dz,
        SpriteProvider sprite,
        Random random
    ) {
        super(world, x, y, z, sprite, random.nextFloat() * .5f);
        velocityX = 0;
        velocityY = 0;
        velocityZ = 0;
        gravityStrength = 0;
        scale = .375f;
        setMaxAge(21);
        setPos(x, y, z);
        zRotation = lastZRotation = random.nextFloat() * MathHelper.PI;
        yaw = (float) MathHelper.atan2(dx, dz) - MathHelper.PI;
        pitch = (float) MathHelper.atan2(dy, Math.sqrt(dx * dx + dz * dz)) - MathHelper.PI / 2;
        this.updateSprite(sprite);
    }

    @Override
    public RenderType getRenderType() {
        return RenderType.PARTICLE_ATLAS_OPAQUE;
    }

    @Override
    public ParticleTextureSheet textureSheet() {
        return SteamJetParticleRenderer.SHEET;
    }

    @Override
    public void render(BillboardParticleSubmittable submittable, Camera camera, float tickProgress) {
        Vec3d vec3 = camera.getPos();
        float f = (float) (x - vec3.x);
        float f1 = (float) (y - vec3.y);
        float f2 = (float) (z - vec3.z);
        float f3 = MathHelper.lerp(tickProgress, lastZRotation, zRotation);
        float f7 = getMinU();
        float f8 = getMaxU();
        float f5 = getMinV();
        float f6 = getMaxV();
        float f4 = getSize(tickProgress);
        RenderType renderType = getRenderType();
        int color = ColorHelper.fromFloats(alpha, red, green, blue);
        int brightness = getBrightness(tickProgress);
        for (int i = 0; i < 4; i++) {
            Quaternionf rotation = RotationAxis.POSITIVE_Y.rotation(yaw);
            rotation.mul(RotationAxis.POSITIVE_X.rotation(pitch));
            rotation.mul(RotationAxis.POSITIVE_Y.rotation(f3 + MathHelper.PI / 2 * i + zRotation));
            submittable.render(renderType, f, f1, f2, rotation.x, rotation.y, rotation.z, rotation.w, f4, f7, f8, f5, f6, color, brightness);
        }
    }

    @Override
    public int getBrightness(float partialTick) {
        BlockPos blockpos = BlockPos.ofFloored(this.x, this.y, this.z);
        return this.world.isPosLoaded(blockpos) ? WorldRenderer.getLightmapCoordinates(world, blockpos) : 0;
    }

    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteSet;

        public Factory(SpriteProvider animatedSprite) {
            this.spriteSet = animatedSprite;
        }

        public Particle createParticle(
            SimpleParticleType type,
            ClientWorld worldIn,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            Random random
        ) {
            return new SteamJetParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet, random);
        }
    }
}

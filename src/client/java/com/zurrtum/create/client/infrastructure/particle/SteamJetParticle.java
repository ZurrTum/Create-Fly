package com.zurrtum.create.client.infrastructure.particle;

import com.zurrtum.create.infrastructure.particle.SteamJetParticleData;
import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SteamJetParticle extends AnimatedParticle {

    private float yaw, pitch;

    protected SteamJetParticle(
        ClientWorld world,
        SteamJetParticleData data,
        double x,
        double y,
        double z,
        double dx,
        double dy,
        double dz,
        SpriteProvider sprite
    ) {
        super(world, x, y, z, sprite, world.random.nextFloat() * .5f);
        velocityX = 0;
        velocityY = 0;
        velocityZ = 0;
        gravityStrength = 0;
        scale = .375f;
        setMaxAge(21);
        setPos(x, y, z);
        zRotation = lastZRotation = world.random.nextFloat() * MathHelper.PI;
        yaw = (float) MathHelper.atan2(dx, dz) - MathHelper.PI;
        pitch = (float) MathHelper.atan2(dy, Math.sqrt(dx * dx + dz * dz)) - MathHelper.PI / 2;
        this.updateSprite(sprite);
    }

    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
    }

    public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
        Vec3d vec3 = pRenderInfo.getPos();
        float f = (float) (x - vec3.x);
        float f1 = (float) (y - vec3.y);
        float f2 = (float) (z - vec3.z);
        float f3 = MathHelper.lerp(pPartialTicks, lastZRotation, zRotation);
        float f7 = this.getMinU();
        float f8 = this.getMaxU();
        float f5 = this.getMinV();
        float f6 = this.getMaxV();
        float f4 = this.getSize(pPartialTicks);

        for (int i = 0; i < 4; i++) {
            Quaternionf quaternion = RotationAxis.POSITIVE_Y.rotation(yaw);
            quaternion.mul(RotationAxis.POSITIVE_X.rotation(pitch));
            quaternion.mul(RotationAxis.POSITIVE_Y.rotation(f3 + MathHelper.PI / 2 * i + zRotation));
            Vector3f vector3f1 = new Vector3f(-1.0F, -1.0F, 0.0F);
            vector3f1.rotate(quaternion);

            Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(
                1.0F,
                1.0F,
                0.0F
            ), new Vector3f(1.0F, -1.0F, 0.0F)};

            for (int j = 0; j < 4; ++j) {
                Vector3f vector3f = avector3f[j];
                vector3f.add(0, 1, 0);
                vector3f.rotate(quaternion);
                vector3f.mul(f4);
                vector3f.add(f, f1, f2);
            }

            int j = this.getBrightness(pPartialTicks);
            pBuffer.vertex(avector3f[0].x(), avector3f[0].y(), avector3f[0].z()).texture(f8, f6).color(this.red, this.green, this.blue, this.alpha)
                .light(j);
            pBuffer.vertex(avector3f[1].x(), avector3f[1].y(), avector3f[1].z()).texture(f8, f5).color(this.red, this.green, this.blue, this.alpha)
                .light(j);
            pBuffer.vertex(avector3f[2].x(), avector3f[2].y(), avector3f[2].z()).texture(f7, f5).color(this.red, this.green, this.blue, this.alpha)
                .light(j);
            pBuffer.vertex(avector3f[3].x(), avector3f[3].y(), avector3f[3].z()).texture(f7, f6).color(this.red, this.green, this.blue, this.alpha)
                .light(j);

        }
    }

    @Override
    public int getBrightness(float partialTick) {
        BlockPos blockpos = BlockPos.ofFloored(this.x, this.y, this.z);
        return this.world.isPosLoaded(blockpos) ? WorldRenderer.getLightmapCoordinates(world, blockpos) : 0;
    }

    public static class Factory implements ParticleFactory<SteamJetParticleData> {
        private final SpriteProvider spriteSet;

        public Factory(SpriteProvider animatedSprite) {
            this.spriteSet = animatedSprite;
        }

        public Particle createParticle(
            SteamJetParticleData data,
            ClientWorld worldIn,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed
        ) {
            return new SteamJetParticle(worldIn, data, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
        }
    }

}

package com.zurrtum.create.client.infrastructure.particle;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.infrastructure.particle.AirParticleData;
import net.minecraft.client.particle.AnimatedParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AirParticle extends AnimatedParticle {

    private float originX, originY, originZ;
    private float targetX, targetY, targetZ;
    private float drag;

    private float twirlRadius, twirlAngleOffset;
    private Direction.Axis twirlAxis;

    protected AirParticle(
        ClientWorld world,
        AirParticleData data,
        double x,
        double y,
        double z,
        double dx,
        double dy,
        double dz,
        SpriteProvider sprite
    ) {
        super(world, x, y, z, sprite, world.random.nextFloat() * .5f);
        scale *= 0.75F;
        collidesWithWorld = false;

        setPos(x, y, z);
        originX = (float) (lastX = x);
        originY = (float) (lastY = y);
        originZ = (float) (lastZ = z);
        targetX = (float) (x + dx);
        targetY = (float) (y + dy);
        targetZ = (float) (z + dz);
        drag = data.drag();

        twirlRadius = world.random.nextFloat() / 6;
        twirlAngleOffset = world.random.nextFloat() * 360;
        twirlAxis = world.random.nextBoolean() ? Direction.Axis.X : Direction.Axis.Z;

        // speed in m/ticks
        double length = new Vec3d(dx, dy, dz).length();
        maxAge = Math.min((int) (length / data.speed()), 60);
        selectSprite(7);
        setAlpha(.25f);

        if (length == 0) {
            markDead();
            setAlpha(0);
        }
    }

    @Override
    public void tick() {
        this.lastX = this.x;
        this.lastY = this.y;
        this.lastZ = this.z;
        if (this.age++ >= this.maxAge) {
            this.markDead();
            return;
        }

        float progress = (float) Math.pow(((float) age) / maxAge, drag);
        float angle = (progress * 2 * 360 + twirlAngleOffset) % 360;
        Vec3d twirl = VecHelper.rotate(new Vec3d(0, twirlRadius, 0), angle, twirlAxis);

        float x = (float) (MathHelper.lerp(progress, originX, targetX) + twirl.x);
        float y = (float) (MathHelper.lerp(progress, originY, targetY) + twirl.y);
        float z = (float) (MathHelper.lerp(progress, originZ, targetZ) + twirl.z);

        velocityX = x - this.x;
        velocityY = y - this.y;
        velocityZ = z - this.z;

        updateSprite(spriteProvider);
        this.move(this.velocityX, this.velocityY, this.velocityZ);
    }

    public int getBrightness(float partialTick) {
        BlockPos blockpos = BlockPos.ofFloored(this.x, this.y, this.z);
        return this.world.isPosLoaded(blockpos) ? WorldRenderer.getLightmapCoordinates(world, blockpos) : 0;
    }

    private void selectSprite(int index) {
        setSprite(spriteProvider.getSprite(index, 8));
    }

    public static class Factory implements ParticleFactory<AirParticleData> {
        private final SpriteProvider spriteSet;

        public Factory(SpriteProvider animatedSprite) {
            this.spriteSet = animatedSprite;
        }

        public Particle createParticle(
            AirParticleData data,
            ClientWorld worldIn,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed
        ) {
            return new AirParticle(worldIn, data, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
        }
    }

}

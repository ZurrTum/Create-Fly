package com.zurrtum.create.client.infrastructure.particle;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.fan.AirCurrent;
import com.zurrtum.create.content.kinetics.fan.IAirCurrentSource;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessingType;
import com.zurrtum.create.infrastructure.particle.AirFlowParticleData;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.particle.*;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AirFlowParticle extends AnimatedParticle {

    private final IAirCurrentSource source;
    private final Access access = new Access();

    protected AirFlowParticle(ClientWorld world, IAirCurrentSource source, double x, double y, double z, SpriteProvider sprite) {
        super(world, x, y, z, sprite, world.random.nextFloat() * .5f);
        this.source = source;
        this.scale *= 0.75F;
        this.maxAge = 40;
        collidesWithWorld = false;
        selectSprite(7);
        Vec3d offset = VecHelper.offsetRandomly(Vec3d.ZERO, random, .25f);
        this.setPos(x + offset.x, y + offset.y, z + offset.z);
        this.lastX = this.x;
        this.lastY = this.y;
        this.lastZ = this.z;
        setColor(0xEEEEEE);
        setAlpha(.25f);
    }

    @NotNull
    public ParticleTextureSheet getRenderType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        if (source == null || source.isSourceRemoved()) {
            markDead();
            return;
        }
        this.lastX = this.x;
        this.lastY = this.y;
        this.lastZ = this.z;
        if (this.age++ >= this.maxAge) {
            markDead();
        } else {
            AirCurrent airCurrent = source.getAirCurrent();
            if (airCurrent == null || !airCurrent.bounds.expand(.25f).contains(x, y, z)) {
                markDead();
                return;
            }

            Vec3d directionVec = Vec3d.of(airCurrent.direction.getVector());
            Vec3d motion = directionVec.multiply(1 / 8f);
            if (!source.getAirCurrent().pushing)
                motion = motion.multiply(-1);

            double distance = new Vec3d(x, y, z).subtract(VecHelper.getCenterOf(source.getAirCurrentPos())).multiply(directionVec).length() - .5f;
            if (distance > airCurrent.maxDistance + 1 || distance < -.25f) {
                markDead();
                return;
            }
            motion = motion.multiply(airCurrent.maxDistance - (distance - 1f)).multiply(.5f);

            FanProcessingType type = getType(distance);
            if (type == null) {
                setColor(0xEEEEEE);
                setAlpha(.25f);
                selectSprite((int) MathHelper.clamp((distance / airCurrent.maxDistance) * 8 + random.nextInt(4), 0, 7));
            } else {
                type.morphAirFlow(access, random);
                selectSprite(random.nextInt(3));
            }

            velocityX = motion.x;
            velocityY = motion.y;
            velocityZ = motion.z;

            if (this.onGround) {
                this.velocityX *= 0.7;
                this.velocityZ *= 0.7;
            }
            this.move(this.velocityX, this.velocityY, this.velocityZ);
        }
    }

    @Nullable
    private FanProcessingType getType(double distance) {
        if (source.getAirCurrent() == null)
            return null;
        return source.getAirCurrent().getTypeAt((float) distance);
    }

    public int getLightColor(float partialTick) {
        BlockPos blockpos = BlockPos.ofFloored(this.x, this.y, this.z);
        return this.world.isPosLoaded(blockpos) ? WorldRenderer.getLightmapCoordinates(world, blockpos) : 0;
    }

    private void selectSprite(int index) {
        setSprite(spriteProvider.getSprite(index, 8));
    }

    public static class Factory implements ParticleFactory<AirFlowParticleData> {
        private final SpriteProvider spriteSet;

        public Factory(SpriteProvider animatedSprite) {
            this.spriteSet = animatedSprite;
        }

        @Override
        public Particle createParticle(
            AirFlowParticleData data,
            ClientWorld worldIn,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed
        ) {
            BlockEntity be = worldIn.getBlockEntity(new BlockPos(data.posX(), data.posY(), data.posZ()));
            if (!(be instanceof IAirCurrentSource))
                be = null;
            return new AirFlowParticle(worldIn, (IAirCurrentSource) be, x, y, z, this.spriteSet);
        }
    }

    private class Access implements FanProcessingType.AirFlowParticleAccess {
        @Override
        public void setColor(int color) {
            AirFlowParticle.this.setColor(color);
        }

        @Override
        public void setAlpha(float alpha) {
            AirFlowParticle.this.setAlpha(alpha);
        }

        @Override
        public void spawnExtraParticle(ParticleEffect options, float speedMultiplier) {
            world.addParticleClient(options, x, y, z, velocityX * speedMultiplier, velocityY * speedMultiplier, velocityZ * speedMultiplier);
        }
    }

}
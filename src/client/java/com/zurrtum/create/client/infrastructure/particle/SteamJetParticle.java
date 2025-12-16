package com.zurrtum.create.client.infrastructure.particle;

import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class SteamJetParticle extends SimpleAnimatedParticle {
    private final float yaw;
    private final float pitch;

    protected SteamJetParticle(
        ClientLevel world,
        double x,
        double y,
        double z,
        double dx,
        double dy,
        double dz,
        SpriteSet sprite,
        RandomSource random
    ) {
        super(world, x, y, z, sprite, random.nextFloat() * .5f);
        xd = 0;
        yd = 0;
        zd = 0;
        gravity = 0;
        quadSize = .375f;
        setLifetime(21);
        setPos(x, y, z);
        roll = oRoll = random.nextFloat() * Mth.PI;
        yaw = (float) Mth.atan2(dx, dz) - Mth.PI;
        pitch = (float) Mth.atan2(dy, Math.sqrt(dx * dx + dz * dz)) - Mth.PI / 2;
        this.setSpriteFromAge(sprite);
    }

    @Override
    public Layer getLayer() {
        return Layer.OPAQUE;
    }

    @Override
    public ParticleRenderType getGroup() {
        return SteamJetParticleRenderer.SHEET;
    }

    @Override
    public void extract(QuadParticleRenderState submittable, Camera camera, float tickProgress) {
        Vec3 vec3 = camera.position();
        float f = (float) (x - vec3.x);
        float f1 = (float) (y - vec3.y);
        float f2 = (float) (z - vec3.z);
        float f3 = Mth.lerp(tickProgress, oRoll, roll);
        float f7 = getU0();
        float f8 = getU1();
        float f5 = getV0();
        float f6 = getV1();
        float f4 = getQuadSize(tickProgress);
        Layer renderType = getLayer();
        int color = ARGB.colorFromFloat(alpha, rCol, gCol, bCol);
        int brightness = getLightCoords(tickProgress);
        for (int i = 0; i < 4; i++) {
            Quaternionf rotation = Axis.YP.rotation(yaw);
            rotation.mul(Axis.XP.rotation(pitch));
            rotation.mul(Axis.YP.rotation(f3 + Mth.PI / 2 * i + roll));
            submittable.add(renderType, f, f1, f2, rotation.x, rotation.y, rotation.z, rotation.w, f4, f7, f8, f5, f6, color, brightness);
        }
    }

    @Override
    public int getLightCoords(float partialTick) {
        BlockPos blockpos = BlockPos.containing(this.x, this.y, this.z);
        return this.level.isLoaded(blockpos) ? LevelRenderer.getLightCoords(level, blockpos) : 0;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet animatedSprite) {
            this.spriteSet = animatedSprite;
        }

        public Particle createParticle(
            SimpleParticleType type,
            ClientLevel worldIn,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            RandomSource random
        ) {
            return new SteamJetParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet, random);
        }
    }
}

package com.zurrtum.create.client.infrastructure.particle;

import com.mojang.math.Axis;
import com.zurrtum.create.AllParticleTypes;
import com.zurrtum.create.client.content.equipment.bell.SoulPulseEffect;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

public class SoulParticle extends CustomRotationParticle {

    protected int startTicks;
    protected int endTicks;
    protected int numLoops;

    protected int firstStartFrame;
    protected int startFrames = 17;

    protected int firstLoopFrame = 17;
    protected int loopFrames = 16;

    protected int firstEndFrame = 33;
    protected int endFrames = 20;

    protected @Nullable AnimationStage animationStage;

    protected int totalFrames = 53;
    protected int ticksPerFrame = 2;

    protected boolean isPerimeter;
    protected boolean isExpandingPerimeter;
    protected boolean isVisible = true;
    protected int perimeterFrames = 8;

    public SoulParticle(
        SimpleParticleType type,
        SpriteSet spriteSet,
        ClientLevel worldIn,
        double x,
        double y,
        double z,
        double vx,
        double vy,
        double vz,
        RandomSource random
    ) {
        super(worldIn, x, y, z, spriteSet, 0);
        quadSize = 0.5f;
        setSize(quadSize, quadSize);

        loopLength = loopFrames + (int) (random.nextFloat() * 5.0f - 4.0f);
        startTicks = startFrames + (int) (random.nextFloat() * 5.0f - 4.0f);
        endTicks = endFrames + (int) (random.nextFloat() * 5.0f - 4.0f);
        numLoops = (int) (1.0f + random.nextFloat() * 2.0f);

        setFrame(0);
        stoppedByCollision = true; // disable movement
        mirror = random.nextBoolean();

        isExpandingPerimeter = type == AllParticleTypes.SOUL_EXPANDING_PERIMETER;
        isPerimeter = type == AllParticleTypes.SOUL_PERIMETER || isExpandingPerimeter;
        animationStage = !isPerimeter ? new StartAnimation(this) : new PerimeterAnimation(this);
        if (isPerimeter) {
            yo = y -= 0.5f - 1 / 128.0f;
            totalFrames = perimeterFrames;
            isVisible = false;
        }
    }

    @Override
    public void tick() {
        animationStage.tick();
        animationStage = animationStage.getNext();

        BlockPos pos = BlockPos.containing(x, y, z);
        if (animationStage == null) {
            remove();
        }
        if (!SoulPulseEffect.isDark(level, pos)) {
            isVisible = true;
            if (!isPerimeter) {
                remove();
            }
        } else if (isPerimeter) {
            isVisible = false;
        }
    }

    @Override
    public void extract(QuadParticleRenderState submittable, Camera camera, float partialTicks) {
        if (!isVisible) {
            return;
        }
        super.extract(submittable, camera, partialTicks);
    }

    public void setFrame(int frame) {
        if (frame >= 0 && frame < totalFrames) {
            setSprite(sprites.get(frame, totalFrames));
        }
    }

    @Override
    public Quaternionf getCustomRotation(Camera camera, float partialTicks) {
        if (isPerimeter) {
            return Axis.XP.rotation(Radians.DEG_270);
        }
        Quaternionf rotation = camera.rotation();
        return new Quaternionf(0, rotation.y, 0, rotation.w);
    }

    public static abstract class AnimationStage {

        protected final SoulParticle particle;

        protected int ticks;
        protected int animAge;

        public AnimationStage(SoulParticle particle) {
            this.particle = particle;
        }

        public void tick() {
            ticks++;

            if (ticks % particle.ticksPerFrame == 0) {
                animAge++;
            }
        }

        public float getAnimAge() {
            return animAge;
        }

        @Nullable
        public abstract AnimationStage getNext();
    }

    public static class StartAnimation extends AnimationStage {

        public StartAnimation(SoulParticle particle) {
            super(particle);
        }

        @Override
        public void tick() {
            super.tick();

            particle.setFrame(particle.firstStartFrame + (int) (getAnimAge() / particle.startTicks * particle.startFrames));
        }

        @Override
        public AnimationStage getNext() {
            if (animAge < particle.startTicks) {
                return this;
            }
            return new LoopAnimation(particle);
        }
    }

    public static class LoopAnimation extends AnimationStage {

        int loops;

        public LoopAnimation(SoulParticle particle) {
            super(particle);
        }

        @Override
        public void tick() {
            super.tick();

            int loopTick = getLoopTick();

            if (loopTick == 0) {
                loops++;
            }

            particle.setFrame(particle.firstLoopFrame + loopTick);// (int) (((float) loopTick / (float)
            // particle.loopLength) * particle.loopFrames));

        }

        private int getLoopTick() {
            return animAge % particle.loopFrames;
        }

        @Override
        public AnimationStage getNext() {
            if (loops <= particle.numLoops) {
                return this;
            }
            return new EndAnimation(particle);
        }
    }

    public static class EndAnimation extends AnimationStage {

        public EndAnimation(SoulParticle particle) {
            super(particle);
        }

        @Override
        public void tick() {
            super.tick();

            particle.setFrame(particle.firstEndFrame + (int) (getAnimAge() / particle.endTicks * particle.endFrames));

        }

        @Override
        @Nullable
        public AnimationStage getNext() {
            if (animAge < particle.endTicks) {
                return this;
            }
            return null;
        }
    }

    public static class PerimeterAnimation extends AnimationStage {

        public PerimeterAnimation(SoulParticle particle) {
            super(particle);
        }

        @Override
        public void tick() {
            super.tick();
            particle.setFrame((int) getAnimAge() % particle.perimeterFrames);
        }

        @Override
        @Nullable
        public AnimationStage getNext() {
            if (animAge < (particle.isExpandingPerimeter ? 8 :
                particle.startTicks + particle.endTicks + particle.numLoops * particle.loopLength)) {
                return this;
            }
            return null;
        }
    }
}

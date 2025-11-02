package com.zurrtum.create.client.infrastructure.particle;

import com.zurrtum.create.AllParticleTypes;
import com.zurrtum.create.client.content.equipment.bell.SoulPulseEffect;
import net.minecraft.client.particle.BillboardParticleSubmittable;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import org.joml.Quaternionf;

public class SoulParticle extends CustomRotationParticle {

    protected int startTicks;
    protected int endTicks;
    protected int numLoops;

    protected int firstStartFrame = 0;
    protected int startFrames = 17;

    protected int firstLoopFrame = 17;
    protected int loopFrames = 16;

    protected int firstEndFrame = 33;
    protected int endFrames = 20;

    protected AnimationStage animationStage;

    protected int totalFrames = 53;
    protected int ticksPerFrame = 2;

    protected boolean isPerimeter = false;
    protected boolean isExpandingPerimeter = false;
    protected boolean isVisible = true;
    protected int perimeterFrames = 8;

    public SoulParticle(
        SimpleParticleType type,
        SpriteProvider spriteSet,
        ClientWorld worldIn,
        double x,
        double y,
        double z,
        double vx,
        double vy,
        double vz,
        Random random
    ) {
        super(worldIn, x, y, z, spriteSet, 0);
        this.scale = 0.5f;
        this.setBoundingBoxSpacing(this.scale, this.scale);

        this.loopLength = loopFrames + (int) (random.nextFloat() * 5f - 4f);
        this.startTicks = startFrames + (int) (random.nextFloat() * 5f - 4f);
        this.endTicks = endFrames + (int) (random.nextFloat() * 5f - 4f);
        this.numLoops = (int) (1f + random.nextFloat() * 2f);

        this.setFrame(0);
        this.stopped = true; // disable movement
        this.mirror = random.nextBoolean();

        this.isExpandingPerimeter = type == AllParticleTypes.SOUL_EXPANDING_PERIMETER;
        this.isPerimeter = type == AllParticleTypes.SOUL_PERIMETER || isExpandingPerimeter;
        this.animationStage = !isPerimeter ? new StartAnimation(this) : new PerimeterAnimation(this);
        if (isPerimeter) {
            lastY = y -= .5f - 1 / 128f;
            totalFrames = perimeterFrames;
            isVisible = false;
        }
    }

    @Override
    public void tick() {
        animationStage.tick();
        animationStage = animationStage.getNext();

        BlockPos pos = BlockPos.ofFloored(x, y, z);
        if (animationStage == null)
            markDead();
        if (!SoulPulseEffect.isDark(world, pos)) {
            isVisible = true;
            if (!isPerimeter)
                markDead();
        } else if (isPerimeter)
            isVisible = false;
    }

    @Override
    public void render(BillboardParticleSubmittable submittable, Camera camera, float partialTicks) {
        if (!isVisible)
            return;
        super.render(submittable, camera, partialTicks);
    }

    public void setFrame(int frame) {
        if (frame >= 0 && frame < totalFrames)
            setSprite(spriteProvider.getSprite(frame, totalFrames));
    }

    @Override
    public Quaternionf getCustomRotation(Camera camera, float partialTicks) {
        if (isPerimeter) {
            return RotationAxis.POSITIVE_X.rotationDegrees(-90);
        }
        Quaternionf rotation = camera.getRotation();
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

            if (ticks % particle.ticksPerFrame == 0)
                animAge++;
        }

        public float getAnimAge() {
            return (float) animAge;
        }

        public abstract AnimationStage getNext();
    }

    public static class StartAnimation extends AnimationStage {

        public StartAnimation(SoulParticle particle) {
            super(particle);
        }

        @Override
        public void tick() {
            super.tick();

            particle.setFrame(particle.firstStartFrame + (int) (getAnimAge() / (float) particle.startTicks * particle.startFrames));
        }

        @Override
        public AnimationStage getNext() {
            if (animAge < particle.startTicks)
                return this;
            else
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

            if (loopTick == 0)
                loops++;

            particle.setFrame(particle.firstLoopFrame + loopTick);// (int) (((float) loopTick / (float)
            // particle.loopLength) * particle.loopFrames));

        }

        private int getLoopTick() {
            return animAge % particle.loopFrames;
        }

        @Override
        public AnimationStage getNext() {
            if (loops <= particle.numLoops)
                return this;
            else
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

            particle.setFrame(particle.firstEndFrame + (int) ((getAnimAge() / (float) particle.endTicks) * particle.endFrames));

        }

        @Override
        public AnimationStage getNext() {
            if (animAge < particle.endTicks)
                return this;
            else
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
        public AnimationStage getNext() {
            if (animAge < (particle.isExpandingPerimeter ? 8 : particle.startTicks + particle.endTicks + particle.numLoops * particle.loopLength))
                return this;
            else
                return null;
        }
    }
}

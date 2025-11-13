package com.zurrtum.create.client.foundation.blockEntity.behaviour.animation;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.content.kinetics.clock.CuckooClockBlockEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class CuckooClockAnimationBehaviour extends AnimationBehaviour<CuckooClockBlockEntity> {
    public LerpedFloat hourHand = LerpedFloat.angular();
    public LerpedFloat minuteHand = LerpedFloat.angular();

    public CuckooClockAnimationBehaviour(CuckooClockBlockEntity be) {
        super(be);
    }

    @Override
    public void tickAnimation() {
        if (blockEntity.getSpeed() == 0)
            return;

        Level world = blockEntity.getLevel();
        int dayTime = (int) (world.getDayTime() % 24000);
        int hours = (dayTime / 1000 + 6) % 24;
        int minutes = (dayTime % 1000) * 60 / 1000;
        moveHands(hours, minutes);

        CuckooClockBlockEntity.Animation animationType = blockEntity.animationType;
        if (animationType == CuckooClockBlockEntity.Animation.NONE) {
            if (AnimationTickHolder.getTicks() % 32 == 0)
                playSound(SoundEvents.NOTE_BLOCK_HAT.value(), 1 / 16f, 2f);
            else if (AnimationTickHolder.getTicks() % 16 == 0)
                playSound(SoundEvents.NOTE_BLOCK_HAT.value(), 1 / 16f, 1.5f);
        } else {
            boolean isSurprise = animationType == CuckooClockBlockEntity.Animation.SURPRISE;
            float value = blockEntity.getAndIncrementProgress();
            if (value > 100)
                animationType = null;

            // sounds

            if (value == 1)
                playSound(SoundEvents.NOTE_BLOCK_CHIME.value(), 2, .5f);
            if (value == 21)
                playSound(SoundEvents.NOTE_BLOCK_CHIME.value(), 2, 0.793701f);

            if (value > 30 && isSurprise) {
                Vec3 pos = VecHelper.offsetRandomly(VecHelper.getCenterOf(blockEntity.getBlockPos()), world.random, .5f);
                world.addParticle(ParticleTypes.LARGE_SMOKE, pos.x, pos.y, pos.z, 0, 0, 0);
            }
            if (value == 40 && isSurprise)
                playSound(SoundEvents.TNT_PRIMED, 1f, 1f);

            int step = isSurprise ? 3 : 15;
            for (int phase = 30; phase <= 60; phase += step) {
                if (value == phase - step / 3)
                    playSound(SoundEvents.CHEST_OPEN, 1 / 16f, 2f);
                if (value == phase) {
                    if (animationType == CuckooClockBlockEntity.Animation.PIG)
                        playSound(SoundEvents.PIG_AMBIENT, 1 / 4f, 1f);
                    else
                        playSound(SoundEvents.CREEPER_HURT, 1 / 4f, 3f);
                }
                if (value == phase + step / 3)
                    playSound(SoundEvents.CHEST_CLOSE, 1 / 16f, 2f);

            }

        }
    }

    private void moveHands(int hours, int minutes) {
        float hourTarget = (float) (360 / 12 * (hours % 12));
        float minuteTarget = (float) (360 / 60 * minutes);

        hourHand.chase(hourTarget, .2f, LerpedFloat.Chaser.EXP);
        minuteHand.chase(minuteTarget, .2f, LerpedFloat.Chaser.EXP);

        hourHand.tickChaser();
        minuteHand.tickChaser();
    }

    private void playSound(SoundEvent sound, float volume, float pitch) {
        Vec3 vec = VecHelper.getCenterOf(blockEntity.getBlockPos());
        blockEntity.getLevel().playLocalSound(vec.x, vec.y, vec.z, sound, SoundSource.BLOCKS, volume, pitch, false);
    }
}

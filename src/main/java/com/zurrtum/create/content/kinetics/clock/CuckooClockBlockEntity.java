package com.zurrtum.create.content.kinetics.clock;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllDamageSources;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import net.minecraft.block.BlockState;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World.ExplosionSourceType;

import java.util.List;
import java.util.Locale;

public class CuckooClockBlockEntity extends KineticBlockEntity {
    public LerpedFloat animationProgress = LerpedFloat.linear();
    public Animation animationType;
    private boolean sendAnimationUpdate;

    public enum Animation implements StringIdentifiable {
        PIG,
        CREEPER,
        SURPRISE,
        NONE;
        public static final Codec<Animation> CODEC = StringIdentifiable.createCodec(Animation::values);

        public String asString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public CuckooClockBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.CUCKOO_CLOCK, pos, state);
        animationType = Animation.NONE;
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.CUCKOO_CLOCK);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        if (clientPacket) {
            view.read("Animation", Animation.CODEC).ifPresent(animation -> {
                animationType = animation;
                animationProgress.startWithValue(0);
            });
        }
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        if (clientPacket && sendAnimationUpdate)
            view.put("Animation", Animation.CODEC, animationType);
        sendAnimationUpdate = false;
        super.write(view, clientPacket);
    }

    @Override
    public void tick() {
        super.tick();
        if (world.isClient() || getSpeed() == 0)
            return;

        boolean isNatural = world.getDimension().natural();
        if (!isNatural) {
            return;
        }

        if (animationType == Animation.NONE) {
            int dayTime = (int) (world.getTimeOfDay() % 24000);
            int hours = (dayTime / 1000 + 6) % 24;
            int minutes = (dayTime % 1000) * 60 / 1000;
            if (hours == 12 && minutes < 5)
                startAnimation(Animation.PIG);
            if (hours == 18 && minutes < 36 && minutes > 31)
                startAnimation(Animation.CREEPER);
        } else {
            float value = getAndIncrementProgress();
            if (value > 100)
                animationType = Animation.NONE;

            if (animationType == Animation.SURPRISE && MathHelper.approximatelyEquals(animationProgress.getValue(), 50)) {
                Vec3d center = VecHelper.getCenterOf(pos);
                world.breakBlock(pos, false);
                world.createExplosion(
                    null,
                    AllDamageSources.get(world).cuckoo_surprise,
                    null,
                    center.x,
                    center.y,
                    center.z,
                    3,
                    false,
                    ExplosionSourceType.BLOCK
                );
            }
        }
    }

    public float getAndIncrementProgress() {
        float value = animationProgress.getValue();
        animationProgress.setValue(value + 1);
        return value;
    }

    public void startAnimation(Animation animation) {
        animationType = animation;
        if (animation != null && CuckooClockBlock.containsSurprise(getCachedState()))
            animationType = Animation.SURPRISE;
        animationProgress.startWithValue(0);
        sendAnimationUpdate = true;

        if (animation == Animation.CREEPER)
            awardIfNear(AllAdvancements.CUCKOO_CLOCK, 32);

        sendData();
    }
}

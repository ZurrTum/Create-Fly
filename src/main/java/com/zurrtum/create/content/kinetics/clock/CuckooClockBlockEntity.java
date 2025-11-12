package com.zurrtum.create.content.kinetics.clock;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllDamageSources;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class CuckooClockBlockEntity extends KineticBlockEntity {
    public LerpedFloat animationProgress = LerpedFloat.linear();
    public Animation animationType;
    private boolean sendAnimationUpdate;

    public enum Animation implements StringRepresentable {
        PIG,
        CREEPER,
        SURPRISE,
        NONE;
        public static final Codec<Animation> CODEC = StringRepresentable.fromEnum(Animation::values);

        public String getSerializedName() {
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
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        if (clientPacket) {
            view.read("Animation", Animation.CODEC).ifPresent(animation -> {
                animationType = animation;
                animationProgress.startWithValue(0);
            });
        }
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        if (clientPacket && sendAnimationUpdate)
            view.store("Animation", Animation.CODEC, animationType);
        sendAnimationUpdate = false;
        super.write(view, clientPacket);
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide() || getSpeed() == 0)
            return;

        boolean isNatural = level.dimensionType().natural();
        if (!isNatural) {
            return;
        }

        if (animationType == Animation.NONE) {
            int dayTime = (int) (level.getDayTime() % 24000);
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

            if (animationType == Animation.SURPRISE && Mth.equal(animationProgress.getValue(), 50)) {
                Vec3 center = VecHelper.getCenterOf(worldPosition);
                level.destroyBlock(worldPosition, false);
                level.explode(
                    null,
                    AllDamageSources.get(level).cuckoo_surprise,
                    null,
                    center.x,
                    center.y,
                    center.z,
                    3,
                    false,
                    ExplosionInteraction.BLOCK
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
        if (animation != null && CuckooClockBlock.containsSurprise(getBlockState()))
            animationType = Animation.SURPRISE;
        animationProgress.startWithValue(0);
        sendAnimationUpdate = true;

        if (animation == Animation.CREEPER)
            awardIfNear(AllAdvancements.CUCKOO_CLOCK, 32);

        sendData();
    }
}

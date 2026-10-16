package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.flywheel.backend.engine.uniform.GameTimeHolder;
import com.zurrtum.create.client.flywheel.backend.engine.uniform.LevelInfoHolder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.timeline.Timeline;
import net.minecraft.world.timeline.Timelines;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(LevelRenderState.class)
public class LevelRenderStateMixin implements LevelInfoHolder {
    @Unique
    private int ticks;
    @Unique
    private int levelDay;
    @Unique
    private float timeOfDay;
    @Unique
    private int skyLight;
    @Unique
    private int raining;
    @Unique
    private int thundering;
    @Unique
    private float thunderLevel;
    @Unique
    private int skyDarken;
    @Unique
    private int constantAmbientLight;
    @Unique
    private int dimensionId;

    @Override
    public void flywheel$update(@Nullable ClientLevel level, float partialTick) {
        if (level == null) {
            setDayTime(0, 0);
            skyLight = 1;
            ticks = raining = thundering = skyDarken = constantAmbientLight = 0;
            thunderLevel = 0;
            dimensionId = -1;
            return;
        }
        ticks = ((GameTimeHolder) level.getLevelData()).flywheel$ticks();
        skyLight = level.dimensionType().hasSkyLight() ? 1 : 0;
        raining = level.isRaining() ? 1 : 0;
        thundering = level.isThundering() ? 1 : 0;
        thunderLevel = level.getThunderLevel(partialTick);
        skyDarken = level.getSkyDarken();
        constantAmbientLight = level.dimensionType().cardinalLightType().ordinal();
        ResourceKey<Level> dimension = level.dimension();
        if (Level.OVERWORLD.equals(dimension)) {
            dimensionId = 0;
        } else if (Level.NETHER.equals(dimension)) {
            dimensionId = 1;
        } else if (Level.END.equals(dimension)) {
            dimensionId = 2;
        } else {
            dimensionId = -1;
        }
        Optional<Holder.Reference<Timeline>> optionalTimeline = level.registryAccess().get(Timelines.OVERWORLD_DAY);
        if (optionalTimeline.isEmpty()) {
            setDayTime(0, 0);
            return;
        }
        Timeline timeline = optionalTimeline.get().value();
        Optional<Integer> optionalPeriodTicks = timeline.periodTicks();
        if (optionalPeriodTicks.isEmpty()) {
            setDayTime(0, 0);
            return;
        }
        int periodTicks = optionalPeriodTicks.get();
        long dayTime = level.clockManager().getInstance(timeline.clock()).totalTicks();
        setDayTime((int) (dayTime / periodTicks % 0x7FFFFFFFL), (float) (dayTime % periodTicks) / periodTicks);
    }

    @Unique
    private void setDayTime(int levelDay, float timeOfDay) {
        this.levelDay = levelDay;
        this.timeOfDay = timeOfDay;
    }

    @Override
    public int flywheel$ticks() {
        return ticks;
    }

    @Override
    public int flywheel$levelDay() {
        return levelDay;
    }

    @Override
    public float flywheel$timeOfDay() {
        return timeOfDay;
    }

    @Override
    public int flywheel$skyLight() {
        return skyLight;
    }

    @Override
    public int flywheel$raining() {
        return raining;
    }

    @Override
    public int flywheel$thundering() {
        return thundering;
    }

    @Override
    public float flywheel$thunderLevel() {
        return thunderLevel;
    }

    @Override
    public int flywheel$skyDarken() {
        return skyDarken;
    }

    @Override
    public int flywheel$constantAmbientLight() {
        return constantAmbientLight;
    }

    @Override
    public int flywheel$dimensionId() {
        return dimensionId;
    }
}

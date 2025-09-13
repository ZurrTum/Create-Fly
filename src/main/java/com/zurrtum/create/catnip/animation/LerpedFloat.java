package com.zurrtum.create.catnip.animation;

import com.zurrtum.create.catnip.math.AngleHelper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class LerpedFloat {

    protected Interpolator interpolator;
    protected float previousValue;
    protected float value;

    @Nullable
    protected Chaser chaseFunction;
    protected float chaseTarget;
    protected float chaseSpeed;
    protected boolean angularChase;

    protected boolean forcedSync;

    public LerpedFloat(Interpolator interpolator) {
        this.interpolator = interpolator;
        startWithValue(0);
        forcedSync = true;
    }

    public static LerpedFloat linear() {
        return new LerpedFloat((p, c, t) -> (float) MathHelper.lerp(p, c, t));
    }

    public static LerpedFloat angular() {
        LerpedFloat lerpedFloat = new LerpedFloat(AngleHelper::angleLerp);
        lerpedFloat.angularChase = true;
        return lerpedFloat;
    }

    public LerpedFloat startWithValue(double value) {
        float f = (float) value;
        this.previousValue = f;
        this.chaseTarget = f;
        this.value = f;
        return this;
    }

    public LerpedFloat chase(double value, double speed, Chaser chaseFunction) {
        updateChaseTarget((float) value);
        this.chaseSpeed = (float) speed;
        this.chaseFunction = chaseFunction;
        return this;
    }

    public LerpedFloat chaseTimed(double value, int ticks) {
        double diff = value - this.value;
        return chase(value, Math.abs(diff / ticks), Chaser.LINEAR);
    }

    public LerpedFloat disableSmartAngleChasing() {
        angularChase = false;
        return this;
    }

    public void updateChaseTarget(float target) {
        if (angularChase)
            target = value + AngleHelper.getShortestAngleDiff(value, target);
        this.chaseTarget = target;
    }

    public boolean updateChaseSpeed(double speed) {
        float prevSpeed = this.chaseSpeed;
        this.chaseSpeed = (float) speed;
        return !MathHelper.approximatelyEquals(prevSpeed, speed);
    }

    public void tickChaser() {
        previousValue = value;
        if (chaseFunction == null)
            return;
        if (MathHelper.approximatelyEquals((double) value, chaseTarget)) {
            value = chaseTarget;
            return;
        }
        value = chaseFunction.chase(value, chaseSpeed, chaseTarget);
    }

    public void setValueNoUpdate(double value) {
        this.value = (float) value;
    }

    public void setValue(double value) {
        this.previousValue = this.value;
        this.value = (float) value;
    }

    public float getValue() {
        return getValue(1);
    }

    public float getValue(float partialTicks) {
        return interpolator.interpolate(partialTicks, previousValue, value);
    }

    public boolean settled() {
        return MathHelper.approximatelyEquals(
            (double) previousValue,
            value
        ) && (chaseFunction == null || MathHelper.approximatelyEquals((double) value, chaseTarget));
    }

    public float getChaseTarget() {
        return chaseTarget;
    }

    public void forceNextSync() {
        forcedSync = true;
    }

    public void write(WriteView view) {
        view.putFloat("Speed", chaseSpeed);
        view.putFloat("Target", chaseTarget);
        view.putFloat("Value", value);
        if (forcedSync)
            view.putBoolean("Force", true);
        forcedSync = false;
    }

    public void read(ReadView view, boolean clientPacket) {
        if (!clientPacket || view.getBoolean("Force", false))
            startWithValue(view.getFloat("Value", 0));
        readChaser(view);
    }

    protected void readChaser(ReadView view) {
        chaseSpeed = view.getFloat("Speed", 0);
        chaseTarget = view.getFloat("Target", 0);
    }

    @FunctionalInterface
    public interface Interpolator {
        float interpolate(double progress, double current, double target);
    }

    @FunctionalInterface
    public interface Chaser {

        Chaser IDLE = (c, s, t) -> (float) c;
        Chaser EXP = exp(Double.MAX_VALUE);
        Chaser LINEAR = (c, s, t) -> (float) (c + MathHelper.clamp(t - c, -s, s));

        static Chaser exp(double maxEffectiveSpeed) {
            return (c, s, t) -> (float) (c + MathHelper.clamp((t - c) * s, -maxEffectiveSpeed, maxEffectiveSpeed));
        }

        float chase(double current, double speed, double target);
    }

}

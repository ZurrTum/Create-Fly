package com.zurrtum.create.content.kinetics.base;

import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.BlockState;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.WorldView;

public interface IRotate extends IWrenchable {

    boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face);

    Axis getRotationAxis(BlockState state);

    default SpeedLevel getMinimumRequiredSpeedLevel() {
        return SpeedLevel.NONE;
    }

    default boolean hideStressImpact() {
        return false;
    }

    default boolean showCapacityWithAnnotation() {
        return false;
    }

    enum SpeedLevel {
        NONE(Formatting.DARK_GRAY, 0x000000, 0),
        SLOW(Formatting.GREEN, 0x22FF22, 10),
        MEDIUM(Formatting.AQUA, 0x0084FF, 20),
        FAST(Formatting.LIGHT_PURPLE, 0xFF55FF, 30);

        private final Formatting textColor;
        private final int color;
        private final int particleSpeed;

        SpeedLevel(Formatting textColor, int color, int particleSpeed) {
            this.textColor = textColor;
            this.color = color;
            this.particleSpeed = particleSpeed;
        }

        public static SpeedLevel of(float speed) {
            speed = Math.abs(speed);

            if (speed >= AllConfigs.server().kinetics.fastSpeed.get())
                return FAST;
            if (speed >= AllConfigs.server().kinetics.mediumSpeed.get())
                return MEDIUM;
            if (speed >= 1)
                return SLOW;
            return NONE;
        }

        public Formatting getTextColor() {
            return textColor;
        }

        public int getColor() {
            return color;
        }

        public int getParticleSpeed() {
            return particleSpeed;
        }

        public float getSpeedValue() {
            return switch (this) {
                case FAST -> AllConfigs.server().kinetics.fastSpeed.get();
                case MEDIUM -> AllConfigs.server().kinetics.mediumSpeed.get();
                case SLOW -> 1;
                default -> 0;
            };
        }

    }

    enum StressImpact {
        LOW(Formatting.YELLOW, Formatting.GREEN),
        MEDIUM(Formatting.GOLD, Formatting.YELLOW),
        HIGH(Formatting.RED, Formatting.GOLD),
        OVERSTRESSED(Formatting.RED, Formatting.RED);

        private final Formatting absoluteColor;
        private final Formatting relativeColor;

        StressImpact(Formatting absoluteColor, Formatting relativeColor) {
            this.absoluteColor = absoluteColor;
            this.relativeColor = relativeColor;
        }

        public static StressImpact of(double stressPercent) {
            if (stressPercent > 1)
                return StressImpact.OVERSTRESSED;
            if (stressPercent > .75d)
                return StressImpact.HIGH;
            if (stressPercent > .5d)
                return StressImpact.MEDIUM;
            return StressImpact.LOW;
        }

        public static boolean isEnabled() {
            return !AllConfigs.server().kinetics.disableStress.get();
        }

        public Formatting getAbsoluteColor() {
            return absoluteColor;
        }

        public Formatting getRelativeColor() {
            return relativeColor;
        }
    }

}

package com.zurrtum.create.content.kinetics.base;

import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public interface IRotate extends IWrenchable {

    boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face);

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
        NONE(ChatFormatting.DARK_GRAY, 0x000000, 0),
        SLOW(ChatFormatting.GREEN, 0x22FF22, 10),
        MEDIUM(ChatFormatting.AQUA, 0x0084FF, 20),
        FAST(ChatFormatting.LIGHT_PURPLE, 0xFF55FF, 30);

        private final ChatFormatting textColor;
        private final int color;
        private final int particleSpeed;

        SpeedLevel(ChatFormatting textColor, int color, int particleSpeed) {
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

        public ChatFormatting getTextColor() {
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
        LOW(ChatFormatting.YELLOW, ChatFormatting.GREEN),
        MEDIUM(ChatFormatting.GOLD, ChatFormatting.YELLOW),
        HIGH(ChatFormatting.RED, ChatFormatting.GOLD),
        OVERSTRESSED(ChatFormatting.RED, ChatFormatting.RED);

        private final ChatFormatting absoluteColor;
        private final ChatFormatting relativeColor;

        StressImpact(ChatFormatting absoluteColor, ChatFormatting relativeColor) {
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

        public ChatFormatting getAbsoluteColor() {
            return absoluteColor;
        }

        public ChatFormatting getRelativeColor() {
            return relativeColor;
        }
    }

}

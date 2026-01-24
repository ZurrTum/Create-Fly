package com.zurrtum.create.content.trains.schedule.condition;

import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.timeline.Timelines;

public class TimeOfDayCondition extends ScheduleWaitCondition {
    public TimeOfDayCondition(Identifier id) {
        super(id);
        data.putInt("Hour", 8);
        data.putInt("Rotation", 5);
    }

    @Override
    public boolean tickCompletion(Level level, Train train, CompoundTag context) {
        int maxTickDiff = 40;
        int targetHour = intData("Hour");
        int targetMinute = intData("Minute");
        int dayTime = (int) (level.getOverworldClockTime() % getRotation());
        int targetTicks = (int) ((((targetHour + 18) % 24) * 1000 + Math.ceil(targetMinute / 60f * 1000)) % getRotation());
        int diff = dayTime - targetTicks;
        return diff >= 0 && maxTickDiff >= diff;
    }

    public int getRotation() {
        int index = intData("Rotation");
        return switch (index) {
            case 9 -> 250;
            case 8 -> 500;
            case 7 -> 750;
            case 6 -> 1000;
            case 5 -> 2000;
            case 4 -> 3000;
            case 3 -> 4000;
            case 2 -> 6000;
            case 1 -> 12000;
            default -> 24000;
        };
    }

    public MutableComponent getDigitalDisplay(int hour, int minute, boolean doubleDigitHrs) {
        int hour12raw = hour % 12 == 0 ? 12 : hour % 12;
        String hr12 = doubleDigitHrs ? twoDigits(hour12raw) : ("" + hour12raw);
        String hr24 = doubleDigitHrs ? twoDigits(hour) : ("" + hour);
        return Component.translatable(
            "create.schedule.condition.time_of_day.digital_format",
            hr12,
            hr24,
            twoDigits(minute),
            hour > 11 ? Component.translatable("create.generic.daytime.pm") : Component.translatable("create.generic.daytime.am")
        );
    }

    public String twoDigits(int t) {
        return t < 10 ? "0" + t : "" + t;
    }

    @Override
    public MutableComponent getWaitingStatus(Level level, Train train, CompoundTag tag) {
        int targetHour = intData("Hour");
        int targetMinute = intData("Minute");
        long timeOfDay = level.getOverworldClockTime();
        int dayTime = (int) (timeOfDay % getRotation());
        int targetTicks = (int) ((((targetHour + 18) % 24) * 1000 + Math.ceil(targetMinute / 60f * 1000)) % getRotation());
        int diff = targetTicks - dayTime;

        if (diff < 0)
            diff += getRotation();

        int periodTicks = level.registryAccess().get(Timelines.OVERWORLD_DAY).flatMap(timeline -> timeline.value().periodTicks()).orElse(24000);
        int departureTime = (int) (timeOfDay + diff) % periodTicks;
        int departingHour = (departureTime / 1000 + 6) % 24;
        int departingMinute = (departureTime % 1000) * 60 / 1000;

        return Component.translatable("create.schedule.condition.time_of_day.status")
            .append(getDigitalDisplay(departingHour, departingMinute, false));
    }
}

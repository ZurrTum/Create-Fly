package com.zurrtum.create.content.trains.schedule.condition;

import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public abstract class TimedWaitCondition extends ScheduleWaitCondition {

    public enum TimeUnit {
        TICKS(1, "t"),
        SECONDS(20, "s"),
        MINUTES(20 * 60, "min");

        public final int ticksPer;
        public final String suffix;

        TimeUnit(int ticksPer, String suffix) {
            this.ticksPer = ticksPer;
            this.suffix = suffix;
        }
    }

    protected void requestDisplayIfNecessary(NbtCompound context, int time) {
        int ticksUntilDeparture = totalWaitTicks() - time;
        if (ticksUntilDeparture < 20 * 60 && ticksUntilDeparture % 100 == 0)
            requestStatusToUpdate(context);
        if (ticksUntilDeparture >= 20 * 60 && ticksUntilDeparture % (20 * 60) == 0)
            requestStatusToUpdate(context);
    }

    public int totalWaitTicks() {
        return getValue() * getUnit().ticksPer;
    }

    public TimedWaitCondition(Identifier id) {
        super(id);
        data.putInt("Value", 5);
        data.putInt("TimeUnit", TimeUnit.SECONDS.ordinal());
    }

    public int getValue() {
        return intData("Value");
    }

    public TimeUnit getUnit() {
        return enumData("TimeUnit", TimeUnit.class);
    }

    @Override
    public MutableText getWaitingStatus(World level, Train train, NbtCompound tag) {
        int time = tag.getInt("Time", 0);
        int ticksUntilDeparture = totalWaitTicks() - time;
        boolean showInMinutes = ticksUntilDeparture >= 20 * 60;
        int num = (int) (showInMinutes ? Math.floor(ticksUntilDeparture / (20 * 60f)) : Math.ceil(ticksUntilDeparture / 100f) * 5);
        String key = "generic." + (showInMinutes ? num == 1 ? "daytime.minute" : "unit.minutes" : num == 1 ? "daytime.second" : "unit.seconds");
        return Text.translatable(
            "create.schedule.condition." + id.getPath() + ".status",
            Text.literal(num + " ").append(Text.translatable("create." + key))
        );
    }

}

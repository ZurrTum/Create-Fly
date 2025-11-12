package com.zurrtum.create.content.trains.schedule.condition;

import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public class ScheduledDelay extends TimedWaitCondition {
    public ScheduledDelay(Identifier id) {
        super(id);
    }

    @Override
    public boolean tickCompletion(Level level, Train train, CompoundTag context) {
        int time = context.getIntOr("Time", 0);
        if (time >= totalWaitTicks())
            return true;

        context.putInt("Time", time + 1);
        requestDisplayIfNecessary(context, time);
        return false;
    }
}

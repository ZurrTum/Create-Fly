package com.zurrtum.create.content.trains.schedule.condition;

import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ScheduledDelay extends TimedWaitCondition {
    public ScheduledDelay(Identifier id) {
        super(id);
    }

    @Override
    public boolean tickCompletion(World level, Train train, NbtCompound context) {
        int time = context.getInt("Time", 0);
        if (time >= totalWaitTicks())
            return true;

        context.putInt("Time", time + 1);
        requestDisplayIfNecessary(context, time);
        return false;
    }
}

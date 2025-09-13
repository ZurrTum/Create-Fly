package com.zurrtum.create.content.trains.schedule.condition;

import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public abstract class LazyTickedScheduleCondition extends ScheduleWaitCondition {

    private final int tickRate;

    public LazyTickedScheduleCondition(Identifier id, int tickRate) {
        super(id);
        this.tickRate = tickRate;
    }

    @Override
    public boolean tickCompletion(World level, Train train, NbtCompound context) {
        int time = context.getInt("Time", 0);
        if (time % tickRate == 0) {
            if (lazyTickCompletion(level, train, context))
                return true;
            time = 0;
        }
        context.putInt("Time", time + 1);
        return false;
    }

    protected abstract boolean lazyTickCompletion(World level, Train train, NbtCompound context);

}

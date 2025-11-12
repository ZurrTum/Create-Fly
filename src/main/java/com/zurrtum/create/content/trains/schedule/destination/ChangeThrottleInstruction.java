package com.zurrtum.create.content.trains.schedule.destination;

import com.zurrtum.create.content.trains.graph.DiscoveredPath;
import com.zurrtum.create.content.trains.schedule.ScheduleRuntime;
import com.zurrtum.create.content.trains.schedule.ScheduleRuntime.State;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ChangeThrottleInstruction extends ScheduleInstruction {
    public ChangeThrottleInstruction(Identifier id) {
        super(id);
        data.putInt("Value", 100);
    }

    @Override
    public boolean supportsConditions() {
        return false;
    }

    public float getThrottle() {
        return intData("Value") / 100f;
    }

    @Override
    public @Nullable DiscoveredPath start(ScheduleRuntime runtime, Level level) {
        runtime.train.throttle = getThrottle();
        runtime.state = State.PRE_TRANSIT;
        runtime.currentEntry++;
        return null;
    }
}

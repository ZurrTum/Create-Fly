package com.zurrtum.create.content.trains.schedule.destination;

import com.zurrtum.create.content.trains.graph.DiscoveredPath;
import com.zurrtum.create.content.trains.schedule.ScheduleRuntime;
import com.zurrtum.create.content.trains.schedule.ScheduleRuntime.State;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ChangeTitleInstruction extends TextScheduleInstruction {
    public ChangeTitleInstruction(Identifier id) {
        super(id);
    }

    @Override
    public boolean supportsConditions() {
        return false;
    }

    public String getScheduleTitle() {
        return getLabelText();
    }

    @Override
    @Nullable
    public DiscoveredPath start(ScheduleRuntime runtime, World level) {
        runtime.currentTitle = getScheduleTitle();
        runtime.state = State.PRE_TRANSIT;
        runtime.currentEntry++;
        return null;
    }
}

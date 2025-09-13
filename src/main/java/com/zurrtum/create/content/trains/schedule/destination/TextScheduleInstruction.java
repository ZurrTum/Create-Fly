package com.zurrtum.create.content.trains.schedule.destination;

import net.minecraft.util.Identifier;

public abstract class TextScheduleInstruction extends ScheduleInstruction {
    public TextScheduleInstruction(Identifier id) {
        super(id);
    }

    public String getLabelText() {
        return textData("Text");
    }
}

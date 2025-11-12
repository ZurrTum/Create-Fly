package com.zurrtum.create.content.trains.schedule.destination;

import net.minecraft.resources.ResourceLocation;

public abstract class TextScheduleInstruction extends ScheduleInstruction {
    public TextScheduleInstruction(ResourceLocation id) {
        super(id);
    }

    public String getLabelText() {
        return textData("Text");
    }
}

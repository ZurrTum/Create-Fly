package com.zurrtum.create.client.content.trains.schedule.condition;

import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.trains.schedule.condition.ScheduledDelay;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class ScheduledDelayRender extends TimedWaitConditionRender<ScheduledDelay> {
    @Override
    public Pair<ItemStack, Text> getSummary(ScheduledDelay input) {
        return Pair.of(ItemStack.EMPTY, CreateLang.translateDirect("schedule.condition.delay_short", formatTime(input, true)));
    }
}

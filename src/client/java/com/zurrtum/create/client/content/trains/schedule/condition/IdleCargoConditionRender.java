package com.zurrtum.create.client.content.trains.schedule.condition;

import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.trains.schedule.condition.IdleCargoCondition;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class IdleCargoConditionRender extends TimedWaitConditionRender<IdleCargoCondition> {
    @Override
    public Pair<ItemStack, Text> getSummary(IdleCargoCondition input) {
        return Pair.of(ItemStack.EMPTY, CreateLang.translateDirect("schedule.condition.idle_short", formatTime(input, true)));
    }
}

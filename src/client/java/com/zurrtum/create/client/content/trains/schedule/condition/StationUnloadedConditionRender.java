package com.zurrtum.create.client.content.trains.schedule.condition;

import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.content.trains.schedule.IScheduleInput;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.trains.schedule.condition.StationUnloadedCondition;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class StationUnloadedConditionRender implements IScheduleInput<StationUnloadedCondition> {
    @Override
    public Pair<ItemStack, Component> getSummary(StationUnloadedCondition input) {
        return Pair.of(ItemStack.EMPTY, CreateLang.translateDirect("schedule.condition.unloaded"));
    }
}

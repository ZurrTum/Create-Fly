package com.zurrtum.create.client.content.trains.schedule.condition;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.content.trains.schedule.IScheduleInput;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.trains.schedule.condition.CargoThresholdCondition;
import com.zurrtum.create.content.trains.schedule.condition.CargoThresholdCondition.Ops;

import java.util.Arrays;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public abstract class CargoThresholdConditionRender<T extends CargoThresholdCondition> implements IScheduleInput<T> {
    protected abstract Component getUnit(T input);

    protected abstract ItemStack getIcon(T input);

    @Override
    public Pair<ItemStack, Component> getSummary(T input) {
        return Pair.of(getIcon(input), Component.literal(input.getOperator().formatted + " " + input.getThreshold()).append(getUnit(input)));
    }

    @Override
    public int slotsTargeted() {
        return 1;
    }

    @Override
    public List<Component> getSecondLineTooltip(int slot) {
        return ImmutableList.of(
            CreateLang.translateDirect("schedule.condition.threshold.place_item"),
            CreateLang.translateDirect("schedule.condition.threshold.place_item_2").withStyle(ChatFormatting.GRAY),
            CreateLang.translateDirect("schedule.condition.threshold.place_item_3").withStyle(ChatFormatting.GRAY)
        );
    }

    public List<MutableComponent> getOpsOptions() {
        return Arrays.stream(Ops.values()).map(op -> CreateLang.translateDirect("schedule.condition.threshold." + Lang.asId(op.name()))).toList();
    }

    @Override
    public void initConfigurationWidgets(T input, ModularGuiLineBuilder builder) {
        builder.addSelectionScrollInput(
            0, 24, (i, l) -> {
                i.forOptions(getOpsOptions()).titled(CreateLang.translateDirect("schedule.condition.threshold.train_holds", ""))
                    .format(state -> Component.literal(" " + Ops.values()[state].formatted));
            }, "Operator"
        );
        builder.addIntegerTextInput(
            29, 41, (e, t) -> {
            }, "Threshold"
        );
    }
}

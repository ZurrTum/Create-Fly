package com.zurrtum.create.client.content.trains.schedule.condition;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.content.trains.schedule.IScheduleInput;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.trains.schedule.condition.CargoThresholdCondition;
import com.zurrtum.create.content.trains.schedule.condition.CargoThresholdCondition.Ops;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.List;

public abstract class CargoThresholdConditionRender<T extends CargoThresholdCondition> implements IScheduleInput<T> {
    protected abstract Text getUnit(T input);

    protected abstract ItemStack getIcon(T input);

    @Override
    public Pair<ItemStack, Text> getSummary(T input) {
        return Pair.of(getIcon(input), Text.literal(input.getOperator().formatted + " " + input.getThreshold()).append(getUnit(input)));
    }

    @Override
    public int slotsTargeted() {
        return 1;
    }

    @Override
    public List<Text> getSecondLineTooltip(int slot) {
        return ImmutableList.of(
            CreateLang.translateDirect("schedule.condition.threshold.place_item"),
            CreateLang.translateDirect("schedule.condition.threshold.place_item_2").formatted(Formatting.GRAY),
            CreateLang.translateDirect("schedule.condition.threshold.place_item_3").formatted(Formatting.GRAY)
        );
    }

    public List<MutableText> getOpsOptions() {
        return Arrays.stream(Ops.values()).map(op -> CreateLang.translateDirect("schedule.condition.threshold." + Lang.asId(op.name()))).toList();
    }

    @Override
    public void initConfigurationWidgets(T input, ModularGuiLineBuilder builder) {
        builder.addSelectionScrollInput(
            0, 24, (i, l) -> {
                i.forOptions(getOpsOptions()).titled(CreateLang.translateDirect("schedule.condition.threshold.train_holds", ""))
                    .format(state -> Text.literal(" " + Ops.values()[state].formatted));
            }, "Operator"
        );
        builder.addIntegerTextInput(
            29, 41, (e, t) -> {
            }, "Threshold"
        );
    }
}
